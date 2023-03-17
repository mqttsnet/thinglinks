1.linux环境运行deploy.sh需要设置文件格式
vi deploy.sh #编辑查看 处理的文件
:set ff=unix #设置文件格式
:wq #保存

2.mysql需要增加volumes：
- ./mysql/db:/docker-entrypoint-initdb.d


172.29.0.1


sed -i "s/172.29.0.1/172.29.0.20/g" `grep 172.29.0.1 -rl .`