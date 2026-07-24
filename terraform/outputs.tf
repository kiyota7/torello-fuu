output "instance_id" {
  description = "EC2インスタンスID(aws ssm send-command等で使用)"
  value       = aws_instance.app.id
}

output "public_ip" {
  description = "アプリへアクセスするためのElastic IP"
  value       = aws_eip.app.public_ip
}

output "rds_endpoint" {
  description = "RDS(PostgreSQL)のエンドポイント(EC2からのみ到達可能)"
  value       = aws_db_instance.app.address
}
