#!/usr/bin/env python3
"""Allow only this deployment runner, then remove only this run's SSH rule."""

import ipaddress
import json
import os
import subprocess
import sys
import urllib.request


def aws_ec2(region, operation, *arguments):
    result = subprocess.run(
        ["aws", "ec2", operation, "--region", region, *arguments, "--output", "json"],
        check=True,
        capture_output=True,
        text=True,
    )
    return json.loads(result.stdout)


def run(mode):
    group = os.environ["EC2_SECURITY_GROUP_ID"]
    region = os.environ["AWS_REGION"]
    run_id = os.environ["GITHUB_RUN_ID"]
    attempt = os.environ["GITHUB_RUN_ATTEMPT"]
    if not group.startswith("sg-") or not group[3:] or any(
        character not in "0123456789abcdef" for character in group[3:]
    ):
        raise ValueError("ALBA_EC2_SECURITY_GROUP_ID must be an EC2 security group ID")
    if not run_id.isdecimal() or not attempt.isdecimal():
        raise ValueError("Invalid GitHub run identity")
    description = f"alba-gha-{run_id}-{attempt}"

    if mode == "open":
        with urllib.request.urlopen("https://checkip.amazonaws.com", timeout=10) as response:
            address = str(ipaddress.IPv4Address(response.read(128).decode("ascii").strip()))
        if not ipaddress.IPv4Address(address).is_global:
            raise ValueError("Runner must have a public IPv4 address")
        permission = [{
            "IpProtocol": "tcp", "FromPort": 22, "ToPort": 22,
            "IpRanges": [{"CidrIp": f"{address}/32", "Description": description}],
        }]
        aws_ec2(region, "authorize-security-group-ingress", "--group-id", group,
                "--ip-permissions", json.dumps(permission))
        print("Temporary SSH access created for this runner (/32).")
    elif mode == "close":
        def own_rules():
            result = aws_ec2(region, "describe-security-group-rules", "--filters",
                             f"Name=group-id,Values={group}")
            return [rule["SecurityGroupRuleId"] for rule in result["SecurityGroupRules"]
                    if rule.get("GroupId") == group
                    and rule.get("Description") == description
                    and rule.get("IsEgress") is False
                    and rule.get("IpProtocol") == "tcp"
                    and rule.get("FromPort") == 22 and rule.get("ToPort") == 22]

        for rule_id in own_rules():
            aws_ec2(region, "revoke-security-group-ingress", "--group-id", group,
                    "--security-group-rule-ids", rule_id)
        if own_rules():
            raise RuntimeError("Temporary SSH rule still exists; check the security group")
        print("This run's temporary SSH access is removed.")
    else:
        raise ValueError("Usage: temporary_ssh.py open|close")


if __name__ == "__main__":
    try:
        run(sys.argv[1] if len(sys.argv) == 2 else "")
    except subprocess.CalledProcessError as error:
        print(error.stderr, file=sys.stderr)
        sys.exit(error.returncode)
