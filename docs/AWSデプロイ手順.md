# AWSデプロイ手順(初心者向け解説つき)

このドキュメントは、タスクボードアプリをAWS上で動かすための手順書であると同時に、
AWS・Terraform・IaC(Infrastructure as Code)を初めて扱う人向けの解説を兼ねている。
AWSマネジメントコンソールを手動で操作するのではなく、**AWS CLIとTerraformというコードを使って**
インフラを構築・デプロイする方針で進める。

## 1. 全体像

### 1-1. 出来上がる構成

```
[あなたのPC]
  │ terraform apply / aws ssm send-command (すべてコマンドライン操作)
  ▼
[EC2インスタンス 1台] (t3.micro, Amazon Linux 2023)
  └─ Docker Compose
       ├─ frontend コンテナ (Nginx: Reactの画面を配信 + /api を backend へ中継)
       └─ backend コンテナ (Spring Bootのアプリ本体)
            │ (5432番のみ、EC2のセキュリティグループからしか繋がらない)
            ▼
       RDS(PostgreSQL, マネージドDB。publicly_accessible=false)
```

データベースは当初EC2内のDockerコンテナで動かしていたが、AWSのマネージドDBサービスである
**RDS**に置き換えた。RDSは`publicly_accessible = false`に設定し、セキュリティグループでも
**EC2のセキュリティグループ経由の5432番のみ**を許可しているため、インターネットからはもちろん、
EC2以外のAWSリソースからも一切接続できない。

### 1-2. 用語の整理(超入門)

- **AWS(Amazon Web Services)**: Amazonが提供するクラウドサービス群。自分でサーバー機材を買わなくても、
  必要な分だけ「仮想のサーバー」や「保存領域」を時間単位で借りられる。
- **EC2(Elastic Compute Cloud)**: AWS上で借りられる「仮想サーバー」そのもの。今回はこの中に
  Dockerを入れてアプリを動かす。
- **EBS(Elastic Block Store)**: EC2に取り付ける「ディスク(ハードディスク)」。
- **VPC(Virtual Private Cloud)**: AWS上に作る仮想のネットワーク(自宅の中のLANのようなもの)。
  今回はアカウントに最初から用意されている「デフォルトVPC」をそのまま使う。
- **セキュリティグループ**: EC2の前に立つ「門番」。どのポート番号への通信を許可するかを制御する
  (今回は80番=Webアクセスのみ許可し、それ以外は塞ぐ)。
- **IAM(Identity and Access Management)**: 「誰が」「何をしてよいか」を管理する権限の仕組み。
- **SSM(AWS Systems Manager) Session Manager**: EC2にSSH鍵なしで安全にログイン・コマンド実行できる仕組み。
  今回はこれを使うため、SSH用のポート(22番)は一切開けない。

### 1-3. IaC(Infrastructure as Code)とは

従来、サーバーの用意は「AWSのWeb画面(マネジメントコンソール)を開いて、ポチポチクリックして
設定する」ことが多かった。しかしこの方法には次のような問題がある。

- 手順を忘れると同じ環境を再現できない
- 誰が何を変更したか記録に残らない
- 「なぜかコンソールの設定が本番と違う」といった環境差異(ドリフト)が起きやすい

**IaC**は、インフラの構成を「コード」として書き、それを実行することでインフラを構築する考え方。
コードなのでGitで変更履歴を管理でき、レビューもでき、何度でも同じ環境を再現できる。
今回使う**Terraform**は、IaCを実現するための代表的なツールの一つ。

### 1-4. Terraformとは

Terraformは「こういう状態のインフラが欲しい」という**あるべき姿(宣言的な設定)**を`.tf`ファイル
(HCLという言語で記述)に書き、それを元にAWS等のクラウド上へ実際にリソースを作成・変更・削除
してくれるツール。

主なコマンド:

| コマンド | 役割 |
|---|---|
| `terraform init` | 必要なプラグイン(プロバイダ)をダウンロードし、作業ディレクトリを初期化する |
| `terraform plan` | 実際に変更を加える前に、「何が作成/変更/削除されるか」の予定表を表示する |
| `terraform apply` | `plan`の内容を実際にAWS上へ適用する |
| `terraform destroy` | Terraformで作ったリソースをすべて削除する(後片付け・課金停止に使う) |

Terraformは`terraform.tfstate`という「今どんなリソースが存在しているか」を記録するファイル
(state)を使って、実際のAWS上の状態とコードの差分を計算している。

## 2. 事前準備(ユーザー自身が行う作業)

以下は認証情報を扱うため、必ずご自身の手で実施すること(このドキュメントの手順に沿って
コマンドを実行すればよい)。

### 2-1. AWS CLIのインストール

このマシンはHomebrewを使わない方針([ローカル開発環境セットアップ](ローカル開発環境セットアップ.md)参照)
のため、AWS公式の`.pkg`インストーラを使う(すでに`node-v24.18.0.pkg`等を同様の方法で導入済み)。

1. [AWS CLI公式サイト](https://awscli.amazonaws.com/AWSCLIV2.pkg)から`AWSCLIV2.pkg`をダウンロードする
2. ダウンロードした`.pkg`をダブルクリックし、画面の指示に従ってインストールする

Homebrewが使える環境であれば、以下でも良い。

```bash
brew install awscli
```

インストール確認:

```bash
aws --version
```

### 2-2. IAMユーザーとアクセスキーの用意

Terraformやaws CLIからAWSを操作するには、「アクセスキーID」と「シークレットアクセスキー」が必要。
ルートアカウント(サインアップ時のアカウント)を直接使うのは推奨されないため、作業用のIAMユーザーを
作成し、そのユーザーのアクセスキーを使う。

1. AWSマネジメントコンソールにサインインする(これは初回のIAMユーザー作成のみで、以降の作業は
   CLIで行う)
2. IAM → ユーザー → 「ユーザーを作成」
3. ユーザー名を入力(例: `terraform-cli`)
4. 許可の設定で、学習目的であれば`AdministratorAccess`ポリシーを付与する
   (本来は必要最小限の権限に絞るのが望ましいが、初学習段階では動作確認を優先する。
   本番運用する場合は権限を絞ることを推奨)
5. 作成後、そのユーザーの「セキュリティ認証情報」タブから「アクセスキーを作成」し、
   アクセスキーID・シークレットアクセスキーを控える(この画面を離れると二度と表示されないため注意)

### 2-3. `aws configure`で認証設定

```bash
aws configure
```

対話式で以下を聞かれるので入力する。

```
AWS Access Key ID [None]: (控えたアクセスキーID)
AWS Secret Access Key [None]: (控えたシークレットアクセスキー)
Default region name [None]: ap-northeast-1
Default output format [None]: json
```

設定確認:

```bash
aws sts get-caller-identity
```

自分のIAMユーザー情報が表示されればOK。

### 2-4. Terraformのインストール

Java/Mavenと同様、sudo不要なzip展開でインストールできる。

```bash
mkdir -p ~/dev-tools
cd ~/dev-tools
curl -O https://releases.hashicorp.com/terraform/1.9.8/terraform_1.9.8_darwin_arm64.zip
unzip terraform_1.9.8_darwin_arm64.zip
```

`~/.zshrc`に`~/dev-tools`をPATHへ追加(Java/Maven導入時に追加済みであれば不要)。

```bash
echo 'export PATH="$HOME/dev-tools:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

Homebrewが使える環境であれば、以下でも良い。

```bash
brew install terraform
```

インストール確認:

```bash
terraform -version
```

## 3. terraform.tfvarsの作成

```bash
cd terraform
cp terraform.tfvars.example terraform.tfvars
```

`terraform.tfvars`を開き、`db_password`を任意の値に変更する。このファイルは`.gitignore`対象
のためコミットされない(パスワードのような秘密情報をGitに残さない、というIaCの基本の一つ)。

## 4. デプロイの実行

```bash
cd terraform
terraform init    # 初回のみ。AWSプロバイダのプラグインをダウンロードする
terraform plan    # 何が作成されるかを確認する
terraform apply   # 実際にAWS上にEC2インスタンス等を作成する(確認プロンプトで yes と入力)
```

`apply`が完了すると、以下のような出力が表示される。

```
Outputs:

instance_id = "i-xxxxxxxxxxxxxxxxx"
public_ip   = "xx.xx.xx.xx"
```

EC2インスタンスの起動スクリプト(user_data)が、内部で以下を自動的に行う。

1. スワップ領域(2GB)の作成(無料利用枠のインスタンスはメモリが1GBしかなく、
   ビルド時にメモリ不足で失敗するのを防ぐため)
2. Docker・Docker Compose・Gitのインストール
3. GitHubリポジトリのclone
4. `.env`ファイルの生成(`db_password`をもとに)
5. `docker compose -f docker-compose.prod.yml up -d --build`によるコンテナの起動

初回はコンテナのビルドに数分かかるため、起動直後はまだアクセスできないことがある。
1〜2分待ってから次の動作確認に進むこと。

## 5. 動作確認

```bash
# バックエンドのヘルスチェック
curl http://<public_ip>/api/health
# => {"status":"ok"} が返ればOK

# ブラウザで開く
open http://<public_ip>/
```

## 6. コード変更時の再デプロイ

コードを変更してGitHubにpushした後、以下を実行するとEC2上に反映される
(SSHもマネジメントコンソールも使わない、AWS CLI(SSM Run Command)だけの操作)。

```bash
./scripts/deploy.sh
```

内部では`aws ssm send-command`を使い、EC2上で`git pull`と
`docker compose up -d --build`を実行している。

## 7. 後片付け(コスト管理)

学習目的で一時的に動かす場合、使い終わったら以下でリソースを削除し、課金を止めることができる。

```bash
cd terraform
terraform destroy
```

### コスト面の注意点

- **EC2(t3.micro)**: 無料利用枠の対象(月750時間まで、アカウント作成から12ヶ月)。動かしっぱなしでも
  1台なら基本的に無料枠に収まる。
- **EBS(8GB gp3)**: 無料利用枠は月30GBまでなので余裕がある。
- **Elastic IP**: インスタンスが起動している間は無料。ただし**インスタンスを停止したままElastic IPだけ
  残すと課金される**ので、長期間使わない場合は`terraform destroy`でElastic IPごと削除すること。
- **RDS(db.t3.micro, 20GB, Single-AZ)**: 無料利用枠の対象(月750時間まで、アカウント作成から12ヶ月。
  EC2の無料枠時間とは別枠でカウントされる)。ストレージも20GBまでは無料利用枠の範囲内。
- **データ転送**: 無料利用枠は月100GBまで(個人の学習・デモ用途では十分)。

## 8. 今回のスコープ外(発展課題)

このデプロイ構成は「最小構成・無料利用枠優先」を優先したための割り切りがある。
興味があれば、次のような発展的な構成にも挑戦できる。

- **HTTPS化**: 独自ドメイン + AWS Certificate Manager(ACM) + ALB(Application Load Balancer)、
  またはCertbotによるLet's Encrypt証明書の導入
- **リモートstate管理**: S3 + DynamoDBを使い、Terraformのstateをチーム間で共有・排他制御する
- **CI/CDによる自動デプロイ**: GitHub Actions等から`terraform apply`や`scripts/deploy.sh`相当の
  処理を自動実行する
- **アプリ側の認証強化**: 現状`SecurityConfig`は全リクエストを許可する暫定設定になっているため、
  ログイン機能実装時に認証必須化する(このデプロイ作業のスコープ外)
