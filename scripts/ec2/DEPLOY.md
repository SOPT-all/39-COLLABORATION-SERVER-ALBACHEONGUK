# EC2 수동 배포

서울 리전의 Ubuntu 24.04 LTS 인스턴스 한 대에서 Spring Boot와 Nginx를 실행합니다.

1. AWS Billing에서 무료 혜택과 크레딧을 확인합니다. 적용 대상이라면 `t3.micro`와 기본 성능의 `gp3` 루트 볼륨을 선택합니다. CPU 크레딧은 Standard, 루트 볼륨은 인스턴스 종료 시 삭제로 설정합니다.
2. 보안 그룹에서 SSH(`22`)는 작업자 IP만, HTTP(`80`)와 HTTPS(`443`)는 외부에서 접근할 수 있게 합니다. API 도메인의 A 레코드를 인스턴스 공인 IPv4에 연결합니다.
3. 로컬에서 `./gradlew bootJar`로 빌드합니다. Windows에서는 `gradlew.bat bootJar`를 사용합니다. `build/libs/app.jar`, `alba.service`, `nginx.conf`를 EC2에 복사합니다.

## Spring Boot 실행

EC2에서 복사한 파일이 있는 디렉터리로 이동한 뒤 실행합니다.

```sh
sudo apt update
sudo apt install openjdk-21-jre-headless nginx
sudo install -d -m 0755 /opt/alba
sudo install -m 0644 app.jar /opt/alba/app.jar
sudo install -m 0644 alba.service /etc/systemd/system/alba.service
sudo systemctl daemon-reload
sudo systemctl enable alba
sudo systemctl restart alba
curl -fsS --retry 20 --retry-connrefused --retry-delay 1 http://127.0.0.1:8080/api/v1/health
```

환경변수가 필요하면 `/etc/alba/alba.env`에 두고 root만 읽을 수 있게 설정합니다.

## Nginx와 HTTPS

`nginx.conf`와 아래 명령의 `api.example.com`을 실제 API 도메인으로 바꿉니다.

```sh
sudo install -m 0644 nginx.conf /etc/nginx/sites-available/alba
sudo ln -sfn /etc/nginx/sites-available/alba /etc/nginx/sites-enabled/alba
sudo nginx -t
sudo systemctl reload nginx

sudo apt install certbot python3-certbot-nginx
sudo certbot --nginx --redirect -d api.example.com
sudo certbot renew --dry-run
curl -I http://api.example.com/api/v1/health
curl -fsS https://api.example.com/api/v1/health
```

외부에서 HTTP 요청이 HTTPS로 리다이렉트되고 HTTPS 헬스체크가 `OK`를 반환하면 프론트 주소에서 API 호출을 확인합니다.

EC2 실행 시간, EBS 용량, 공인 IPv4, 인터넷 송신량은 무료 혜택이나 크레딧을 초과하면 요금이 발생할 수 있습니다. 기본 공인 IPv4는 EC2를 중지했다가 시작할 때 바뀔 수 있으므로, 재시작 후 도메인의 A 레코드를 확인합니다.
