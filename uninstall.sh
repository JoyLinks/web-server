#!/bin/bash

# 检查是否以 root 权限运行
if [ "$(id -u)" -ne 0 ]; then
    echo "请以 root 权限，或使用 sudo 执行"
    exit 1
fi

# 停止服务
systemctl stop joyzl-web-server

# 删除服务
systemctl disable joyzl-web-server.service
rm /lib/systemd/system/joyzl-web-server.service
systemctl daemon-reload

# 删除目录
rm -r /opt/joyzl/web-server
rm -r /var/cache/joyzl/web-server
rm -r /var/log/joyzl/web-server
rm -r /var/lib/joyzl/web-server

# 删除用户和组
userdel joyzl
groupdel joyzl

echo "卸载完成"