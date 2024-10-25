# 📓 CI/CD 배포 및 운영 전략

## 📌 기술 스택

<div align="center">
<img src="https://img.shields.io/badge/linux-FCC624?style=for-the-badge&logo=linux&logoColor=black" style="border-radius: 5px;">
<img src="https://img.shields.io/badge/kubernetes-326CE5?style=for-the-badge&logo=kubernetes&logoColor=white" style="border-radius: 5px;">
<img src="https://img.shields.io/badge/docker-2496ED?style=for-the-badge&logo=docker&logoColor=white" style="border-radius: 5px;">
<img src="https://img.shields.io/badge/argo-EF7B4D?style=for-the-badge&logo=argo&logoColor=white" style="border-radius: 5px;"></br>
<img src="https://img.shields.io/badge/jenkins-D24939?style=for-the-badge&logo=jenkins&logoColor=white" style="border-radius: 5px;">
<img src="https://img.shields.io/badge/git-F05032?style=for-the-badge&logo=git&logoColor=white" style="border-radius: 5px;">
<img src="https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=white" style="border-radius: 5px;">
<img src="https://img.shields.io/badge/webhook-2088FF?style=for-the-badge&logo=webhook&logoColor=white" style="border-radius: 5px;">
</div>
<br>

## 💻 운영 환경

- Master Node: 1
- Worker Node: ?
<br><br>
- OS : Linux
- Container : Docker v
- Orchestration : Kubernetes v
- CI/CD : Jenkins v, Argo cd v
- Monitoring : 
- Notification : Discord
<br><br>

## ✨ 운영 전략
### ✔️ CI/CD 및 자동화
#### 1. Jenkins, Docker: 빌드 및 컨테이너화
- 코드 변경 후 PR 요청 시 Jenkins가 자동으로 빌드 및 테스트를 수행
- Docker image 생성 및 업로드하여 일관된 배포 환경 제공

#### 2. Argo CD: Kubernetes 롤링 업데이트 배포
- GitOps 방식을 활용하여 auto sync를 통해 변경 사항을 Kubernetes 클러스터에 자동으로 배포
- Ingress를 사용하여 IP로 Pod에 연결하고, 모든 Pod는 readinessProbe와 livenessProbe를 사용하여 연결 전 healthcheck를 통해 서비스 연결 상태를 관리

#### 3. 기대 효과
- Rolling Update는 서비스의 중단 없이 Pod를 하나씩 업데이트해 지속적 서비스 제공
- 문제 발생 시 이전 버전으로 빠르게 롤백할 수 있어 서비스 가용성 유지
- Argo CD는 지속적으로 애플리케이션 상태를 모니터링하여, 상태 불일치 시 자동 조정
- Rolling Update는 한 번에 하나의 Pod만 업데이트하여 효율적인 리소스 사용량 관리 가능
- Argo CD와 Kubernetes의 자동 확장 기능을 통해 수요에 따라 자원을 동적으로 할당 가능

<br>

### ✔️ 애플리케이션 아키텍처 및 서비스 운영 전략
#### 1. Backend
- Deployment 사용하여 Kubernetes에서 최소 2개, 최대 4개의 Auto Scaling Pod로 운영
- Rolling Update 방식을 통한 무중단 배포 수행
- 채팅 메시지를 Kafka Broker에 전달, 각 서버가 consume하여 Websocket 세션이 노드에 따라 상이할 때 발생 가능한 채팅 데이터 누락 위험을 방지하고 데이터 일관성 유지
- ClusterIP를 통해 Frontend, Database, Kafka 간 통신

#### 2. Frontend
- Deployment 사용하여 Kubernetes에서 최소 2개, 최대 4개의 Auto Scaling Pod로 운영
- Rolling Update 방식을 통한 무중단 배포 수행
- LoadBalancer 타입의 서비스를 통해 외부와 연결
- Nginx의 Reverse Proxy를 통해 Kubernetes 내부의 Backend Service로 라우팅
- WebSocket 연결 지원을 위해 ws://로 헤더 변경 가능

#### 3. S3 버킷을 통한 이미지 처리
- 파일, 이미지는 AWS S3 버킷을 통해 저장 및 제공
- Backend에서는 이미지를 업로드하면 S3 버킷에 저장하고, 저장된 이미지의 URL을 반환하여 Frontend에서 해당 이미지를 사용자에게 표시
- 파일, 이미지를 서버 로컬에 저장하지 않음으로써 서버 리소스를 절약하고, 높은 가용성 및 확장성을 제공하는 AWS S3를 통해 파일을 효율적으로 관리

<br>

## ✨젠킨스 파이프라인(배포 시나리오)
### Backend
#### 1.	🔗 git push
- 각 브랜치에서 작업 후 PR을 요청, main 브랜치에 push
#### 2.	📝 webhook
- merge 후 Jenkins 서버로 Webhook 요청
#### 3.	📚 Project Clean ~ Build
- GitHub에서 프로젝트 clone
- Spring Boot 프로젝트를 clean하여 기존 target 폴더를 삭제
- mvn compile: 프로젝트 컴파일
- mvn test: 테스트 진행
- mvn package: 배포 가능한 jar 파일 생성
#### 4.	🎁 Docker Build ~ Push
- Dockerfile을 기반으로 docker build르 진행, 새로운 도커 이미지 생성
- 생성된 도커 이미지를 Docker Hub에 push
#### 5.	📌 Docker Image Push
- Docker Hub에 생성된 새 버전의 이미지 업로드
#### 6.	🖌️ Send Artifacts ~ Modify and Apply Deployment
- image 업로드 완료 시, Kubernetes master node에서 backend Manifest 파일 배포
#### 7.	🗓️ K8S Deployment Docker Image Update
- 최신 Docker Image 적용하여 새로운 Deployment 생성
#### 8.	🛎️ Send Discord Webhook
- 배포 성공 또는 실패 시 Discord Webhook으로 알림 전송

<br>

### Frontend
#### 1.	💬 git push
- 각 브랜치에서 작업 후 PR을 요청, main 브랜치에 push
#### 2.	📦 webhook
- merge 후 Jenkins 서버로 Webhook 요청
#### 3.	💊 Install Dependencies
- package.json을 기반으로 npm install 수행
#### 4.	🔧 Project Test ~ Build
- npm run build: dist 폴더 생성
#### 5.	🗄️ Docker Build ~ Push
- Dockerfile을 기반으로 docker build르 진행, 새로운 도커 이미지 생성
- 생성된 도커 이미지를 Docker Hub에 push
#### 6.	📘 Docker Image Push
- Docker Hub에 생성된 새 버전의 이미지 업로드
#### 7.	✏️ Send Artifacts ~ Modify and Apply Deployment
- Kubernetes master node에 Manifest 파일 배포
#### 8.	📥 K8S Deployment Docker Image Update
- 최신 Docker Image 적용하여 새로운 Deployment 생성
#### 9.	🖇️ Send Discord Webhook
- 배포 성공 또는 실패 시 Discord Webhook으로 알림 전송
