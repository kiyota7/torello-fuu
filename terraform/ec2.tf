# AMI IDをハードコードせず、AWSが公式に提供するSSMパラメータから
# 常に最新のAmazon Linux 2023(x86_64)のAMIを動的に取得する。
data "aws_ssm_parameter" "al2023_ami" {
  name = "/aws/service/ami-amazon-linux-latest/al2023-ami-kernel-default-x86_64"
}

resource "aws_instance" "app" {
  ami                    = data.aws_ssm_parameter.al2023_ami.value
  instance_type          = var.instance_type
  subnet_id              = data.aws_subnets.default.ids[0]
  vpc_security_group_ids = [aws_security_group.app.id]
  iam_instance_profile   = aws_iam_instance_profile.ec2_ssm.name

  root_block_device {
    volume_type = "gp3"
    volume_size = 8
  }

  user_data = templatefile("${path.module}/user_data.sh.tpl", {
    repo_url    = var.repo_url
    repo_branch = var.repo_branch
    db_endpoint = aws_db_instance.app.address
    db_password = var.db_password
  })

  tags = {
    Name = "taskboard-app"
  }
}

resource "aws_eip" "app" {
  instance = aws_instance.app.id
  domain   = "vpc"

  tags = {
    Name = "taskboard-app-eip"
  }
}
