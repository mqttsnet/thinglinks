#1.linux环境运行deploy.sh需要设置文件格式
```
vi deploy.sh #编辑查看 处理的文件
:set ff=unix #设置文件格式
:wq #保存
```
#2.启动基础环境
```
1.启动基础环境
sh deploy.sh base
2.查看启动的容器
docker-compose ps -a 
3.查看创建的镜像
docker-compose images
4.重启nacos（由于数据库自动创建表需要时间）
docker-compose restart thinglinks-nacos
```
#3.导入nacos配置
```
1.登录nacos(用户名密码：nacos/nacos)
http://192.168.2.88:8848/nacos
2.创建命名空间
8ea40c2e-64ba-4430-9bd8-a25336b2b45a
导入doc\nacos-config\20230317\DEFAULT_GROUP
3.创建命名空间
1e1aff6c-da73-43e2-9e5f-8e0b890189d9
导入doc\nacos-config\20230317\SEATA_GROUP
```
#4.启动业务程序模块
```
1.启动业务程序模块
sh deploy.sh modules
2.查看启动的容器
docker-compose ps -a 
3.查看创建的镜像
docker-compose images
```
#5.卸载thinglinks
```
1.卸载thinglinks
sh deploy.sh stop
sh deploy.sh rm
2.查看启动的容器
docker-compose ps -a 
3.查看创建的镜像
docker-compose images
```
#6.访问系统
```
1.访问系统（用户名密码：admin/admin123）
http://192.168.2.88/
2.访问rocketmq控制台
http://192.168.2.88:8180/
3.登录nacos控制台(用户名密码：nacos/nacos)
http://192.168.2.88:8848/nacos
```
#一些重用命令
```
1.批量修改当期目录及其子目录文件内容命令
sed -i "s/原字符串/新字符串/g" `grep 原字符串 -rl .`
2.查看日志名
docker logs -f -t --tail 1000 容器名称
3.重启docker容器
docker-compose restart 容器名称
4.列出所有当前主机上或Swarm集群上的网络
docker network ls
5.查看网络详情
docker network inspect network名称
6.清除未使用的docker网络
docker network prune -f
7.查看镜像详情
docker image inspect 镜像名称
8.查看容器详情
docker inspect 容器名称
通过该命令可以获取到容器工作目录对应的源地址

```
##docker容器工作目录对应的源地址
![docker容器详情-容器工作目录对应的源地址](../imgs/docker容器详情.png)


