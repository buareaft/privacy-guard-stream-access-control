# 隐私卫士

端到端的可定制化流数据密文访问控制系统 Java Swing 演示版。

## 功能

- 数据拥有者登录、创建数据流、绑定数据生产者。
- 模拟流数据加密上传，生成密文并写入模拟 Kafka 日志。
- 设置单流隐私策略和联邦隐私策略。
- 管理数据流与隐私策略，支持删除和撤销授权。
- 数据拥有者查询原始授权范围内的数据块与统计信息。
- 数据消费者按隐私策略查询被授权数据，支持联邦统计。
- 隐私控制器和数据服务器日志展示。

## 运行

```powershell
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse -Filter *.java -Path src | ForEach-Object { $_.FullName })
java -cp out privacyguard.Main
```

测试账号：

- 数据拥有者：`owner` / `123456`
- 数据消费者：`consumer` / `123456`
- 隐私控制器：`admin` / `123456`
