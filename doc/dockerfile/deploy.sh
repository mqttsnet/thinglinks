#!/bin/sh

# 使用说明，用来提示输入参数
usage() {
	echo "Usage: sh 执行脚本.sh [port|base|modules|stop|rm]"
	exit 1
}

# 开启所需端口
port(){
	firewall-cmd --add-port=19000/tcp --permanent
	firewall-cmd --add-port=19100/tcp --permanent
	firewall-cmd --add-port=8848/tcp --permanent
	firewall-cmd --add-port=9848/tcp --permanent
	firewall-cmd --add-port=9849/tcp --permanent
	firewall-cmd --add-port=6379/tcp --permanent
	firewall-cmd --add-port=3306/tcp --permanent
	firewall-cmd --add-port=19300/tcp --permanent
	firewall-cmd --add-port=11883/tcp --permanent
	firewall-cmd --add-port=18443/tcp --permanent
	firewall-cmd --add-port=18999/tcp --permanent
	service firewalld restart
}

# 启动基础环境（必须）
base(){
	docker-compose up -d thinglinks-mysql thinglinks-redis thinglinks-nacos
}

# 启动程序模块（必须）
modules(){
	docker-compose up -d thinglinks-nginx thinglinks-gateway thinglinks-auth thinglinks-modules-file thinglinks-modules-gen thinglinks-modules-job thinglinks-modules-system thinglinks-modules-tdengine thinglinks-modules-link thinglinks-modules-broker thinglinks-visual-monitor thinglinks-visual-collection
}

# 关闭所有环境/模块
stop(){
	docker-compose stop
}

# 删除所有环境/模块
rm(){
	docker-compose rm
}

# 根据输入参数，选择执行对应方法，不输入则执行使用说明
case "$1" in
"port")
	port
;;
"base")
	base
;;
"modules")
	modules
;;
"stop")
	stop
;;
"rm")
	rm
;;
*)
	usage
;;
esac
