# 📓 CI/CD 배포 및 운영 전략

## 📌 기술 스택

<div align="center">
<img src="https://img.shields.io/badge/linux-FCC624?style=for-the-badge&logo=linux&logoColor=black" style="border-radius: 5px;">
<img src="https://img.shields.io/badge/kubernetes-326CE5?style=for-the-badge&logo=kubernetes&logoColor=white" style="border-radius: 5px;">
<img src="https://img.shields.io/badge/docker-2496ED?style=for-the-badge&logo=docker&logoColor=white" style="border-radius: 5px;"></br>
<img src="https://img.shields.io/badge/githubactions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white" style="border-radius: 5px;">
<img src="https://img.shields.io/badge/git-F05032?style=for-the-badge&logo=git&logoColor=white" style="border-radius: 5px;">
<img src="https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=white" style="border-radius: 5px;">
<img src="https://img.shields.io/badge/webhook-2088FF?style=for-the-badge&logo=webhook&logoColor=white" style="border-radius: 5px;"></br>
<img src="https://img.shields.io/badge/amazoneks-FF9900?style=for-the-badge&logo=amazoneks&logoColor=white" style="border-radius: 5px;">
<img src="https://img.shields.io/badge/amazonec2-2088FF?style=for-the-badge&logo=amazonec2&logoColor=white" style="border-radius: 5px;">
<img src="https://img.shields.io/badge/amazonroute53-8C4FFF?style=for-the-badge&logo=amazonroute53&logoColor=white" style="border-radius: 5px;">
  
</div>
<br>

## 💻 운영 환경

- Master Node: 1
- Worker Node: 2 (Frontend 1, Backend 1)
<br><br>
- OS : Linux
- Container : Docker
- Orchestration : Amazon EKS(Kubernetes)
- CI/CD : Github Actions
- Monitoring : 
- Notification : Discord
<br><br>

## ✨ 운영 전략
### ✔️ Amazon EKS를 통한 배포
- Amazon Elastic Kubernetes Service(Amazon EKS)는 Amazon Web Services(AWS)에 Kubernetes 컨트롤 플레인을 설치, 운영 및 유지 관리할 필요가 없는 관리형 서비스입니다.
- Kubernetes는 컨테이너화된 애플리케이션의 관리, 규모 조정 및 배포를 자동화하는 오픈 소스 시스템입니다.
#### 🔗 기대 효과
1. **보안 및 인증**: Amazon EKS는 다중 계층 보안 모델을 사용하며, 네트워크 격리, 데이터 암호화 및 사용자 인증 등을 통해 클러스터 보안 강화.
2. **관리성 및 운영**: AWS EKS는 Kubernetes 클러스터의 설치, 운영 및 확장을 간소화하여 클러스터의 배포, 패치, 백업 등을 자동으로 처리할 수 있다. AWS Management Console, AWS CLI, eksctl, Terraform 등을 통해 Kubernetes 클러스터를 관리하고 변경할 수 있다.
3. **확장성 및 규모 조정**: AWS EKS는 Amazon EC2 인스턴스와 함께 자동으로 확장되며, 트래픽 증가에 따라 애플리케이션을 쉽게 확장할 수 있다. AWS Auto Scaling을 사용하여 애플리케이션의 요구에 맞춰 자원을 자동으로 조정할 수 있다. Amazon EKS는 CPU 또는 사용자 지정 지표를 기반으로 수평 Pod 자동 규모 조정 및 전체 워크로드 수요를 기반으로 클러스터 자동 규모 조정을 지원한다.
4. **가용성 및 신뢰성**: Amazon EKS는 여러 가용 영역(AZ)에 걸쳐 고가용성을 지원하여 클러스터와 애플리케이션의 중단 시간을 최소화하고, 재해 복구를 간소화한다. 여러 AZ에 걸친 컨트롤 플레인에 대한 고가용성을 제공하여, 클러스터와 애플리케이션의 중단 시간을 최소화하고 높은 신뢰성을 보장할 수 있다.

### ✔️ CI/CD 및 자동화
#### 1. GitHub Actions, Docker: 빌드 및 컨테이너화
- 코드 변경 후 PR 요청 시 Github Actions가 자동으로 빌드 및 테스트를 수행
- Docker image 생성 및 Amazon ECR에 업로드하여 일관된 배포 환경 제공

#### 2. AWS Route53 설정
- Amazon Route 53을 통한 도메인 등록 및 트래픽 라우팅 설정
- Route 53의 헬스 체크 기능을 활용하여 비정상적인 엔드포인트 감지 및 자동 트래픽 전환 기능 제공
- HTTPS 보안 연결 설정: Amazon Certificate Manager(ACM)를 통한 인증서 생성 및 관리
- 인증서를 EKS의 Ingress 리소스와 연동하여 클러스터에 HTTPS 트래픽 제공

#### 3. 무중단 배포: Kubernetes 롤링 업데이트 배포
- 사용 중인 인스턴스 내에서 새 버전을 점진적으로 교체하여 순차적으로 재배포
- 서비스 운영 간 중단 없이 기능 제공
- 추가 인프라 구성 없이 배포 가능

#### 4. 기대 효과
- Rolling Update는 서비스의 중단 없이 Pod를 하나씩 업데이트해 지속적 서비스 제공
- 문제 발생 시 이전 버전으로 빠르게 롤백할 수 있어 서비스 가용성 유지
- Rolling Update는 한 번에 하나의 Pod만 업데이트하여 효율적인 리소스 사용량 관리 가능
- Kubernetes의 자동 확장 기능을 통해 수요에 따라 자원을 동적으로 할당 가능

<br>

## 📖 시스템 아키텍처
![200ok_architecture](https://github.com/user-attachments/assets/ae49c0a3-15f4-4f92-ae4e-0c9c3e996692)


<br>

### ✔️ 애플리케이션 아키텍처 및 서비스 운영 전략
#### 1. Backend
- Amazon EKS에서 Deployment 사용하여 최소 3개, 최대 4개의 Auto Scaling Pod로 운영
- Rolling Update 방식을 통한 무중단 배포 수행
- 채팅 메시지를 Kafka Broker에 전달, 각 서버가 consume하여 Websocket 세션이 노드에 따라 상이할 때 발생 가능한 채팅 데이터 누락 위험을 방지하고 데이터 일관성 유지
- Ingress를 통해 호스트 기반으로 명령 처리

#### 2. Frontend
- Amazon EKS에서 Deployment 사용하여 최소 3개, 최대 4개의 Auto Scaling Pod로 운영
- Rolling Update 방식을 통한 무중단 배포 수행
- WebSocket 연결 지원을 위해 wss://로 헤더 변경 가능
- Ingress를 통해 호스트 기반으로 명령 처리

#### 3. S3 버킷을 통한 이미지 처리
- 파일, 이미지는 AWS S3 버킷을 통해 저장 및 제공
- Backend에서는 이미지를 업로드하면 S3 버킷에 저장하고, 저장된 이미지의 URL을 반환하여 Frontend에서 해당 이미지를 사용자에게 표시
- 파일, 이미지를 서버 로컬에 저장하지 않음으로써 서버 리소스를 절약하고, 높은 가용성 및 확장성을 제공하는 AWS S3를 통해 파일을 효율적으로 관리

<br>

## ✨GitHub Actions 워크플로우(배포 시나리오)
### Backend
#### 1.	🔗 git push
- 각 브랜치에서 작업 후 PR을 요청, main 브랜치에 push
#### 2.	📚 Project Build
- GitHub 프로젝트 checkout
- 운영 환경에 맞는 JDK version, image setup
- main/resources 디렉토리에 application.yml 파일 생성
- ./gradlew build: 프로젝트 컴파일, 테스트 진행
- 빌드된 jar 파일을 GitHub Actions의 아티팩트 저장소에 업로드
#### 3.	🎁 Docker Build ~ Push
- 업로드된 jar 파일을 가져와 Dockerfile을 기반으로 docker build 진행, 새로운 도커 이미지 생성
- 생성된 도커 이미지를 Amazon ECR에 푸시
#### 4.	🖌️ Modify and Apply K8S Deployment
- image 업로드 완료 후 deploy.yaml 파일을 최신 Docker Image 버전으로 수정하여 Git commit
- workflow의 순환참조 방지를 위해 deploy.yaml 파일 paths-ignore
#### 5.	🗓️ Deploy Backend
- EC2 인스턴스에 SSH 원격 접속을 수행
- kubecel set image 명령어를 통해 기존의 Kubernetes Deployment의 컨테이너 이미지 업데이트
#### 6.	🛎️ Send Discord Webhook
- 배포 성공 시 Discord Webhook으로 알림 전송

<br>

### Frontend
#### 1.	💬 git push
- 각 브랜치에서 작업 후 PR을 요청, main 브랜치에 push
#### 2.	💊 Install Dependencies
- package.json을 기반으로 npm install 수행
#### 3.	🔧 Project Test ~ Build
- npm run build를 통해 프로젝트 빌드 진행
- 생성된 dist 폴더를 Github Actions의 아티팩트 저장소에 업로드
#### 4.	🗄️ Modify and Apply K8S Deployment
- 업로드된 dist 폴더를 가져와 Dockerfile을 기반으로 docker build 진행, 새로운 도커 이미지 생성
- 생성된 도커 이미지를 Amazon ECR에 푸시
#### 5.	📘 Docker Image Push
- image 업로드 완료 후 deploy.yaml 파일을 최신 Docker Image 버전으로 수정하여 Git commit
- workflow의 순환참조 방지를 위해 deploy.yaml 파일 paths-ignore
#### 6.	✏️ Send Artifacts ~ Modify and Apply Deployment
- Kubernetes master node에 Manifest 파일 배포
#### 7.	📥 Deploy Frontend
- EC2 인스턴스에 SSH 원격 접속을 수행
- kubecel set image 명령어를 통해 기존의 Kubernetes Deployment의 컨테이너 이미지 업데이트
#### 8.	🖇️ Send Discord Webhook
- 배포 성공 또는 실패 시 Discord Webhook으로 알림 전송
