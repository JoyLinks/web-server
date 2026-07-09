#!/bin/bash

# 错误时退出脚本
set -e

# 检查是否以 root 权限运行
if [ "$(id -u)" -ne 0 ]; then
    echo "请以 root 权限，或使用 sudo 执行"
    exit 1
fi

# 创建系统组
if ! getent group joyzl > /dev/null; then
	groupadd --system joyzl
	# 创建系统用户，将其主要组设置为 joyzl，禁止登录
	useradd --system --no-create-home --shell /bin/false -g joyzl joyzl

	# 获取的原始用户
	ORIGINAL_USER=${SUDO_USER:-$USER}
	# 将管理用户添加到 joyzl 组
	if id "$ORIGINAL_USER" &>/dev/null; then
	    usermod -a -G joyzl "$ORIGINAL_USER"
	else
	    echo "用户 $ORIGINAL_USER 不存在，无法添加到 joyzl 组"
	fi
fi


# 创建程序目录
mkdir -p /opt/joyzl/web-server
# 创建缓存目录
mkdir -p /var/cache/joyzl/web-server
# 创建数据目录
mkdir -p /var/lib/joyzl/web-server
# 创建日志目录
mkdir -p /var/log/joyzl/web-server
# 创建发布目录
mkdir -p /srv/www
mkdir -p /var/www

# 复制程序文件
cp -rp ./* /opt/joyzl/web-server/
# 移动配置文件
mv -n /opt/joyzl/web-server/server.properties /var/lib/joyzl/web-server/
mv -n /opt/joyzl/web-server/*.json /var/lib/joyzl/web-server/
mv -n /opt/joyzl/web-server/manage /var/lib/joyzl/web-server/
# 删除多余文件
rm /opt/joyzl/web-server/uninstall.sh
rm /opt/joyzl/web-server/install.sh

# 设置目录权限 (root:joyzl, 750/640)
chown -R root:joyzl /opt/joyzl/web-server
chown -R joyzl:joyzl /var/cache/joyzl/web-server
chown -R joyzl:joyzl /var/lib/joyzl/web-server
chown -R joyzl:joyzl /var/log/joyzl/web-server

chmod -R 770 /var/cache/joyzl/web-server
chmod -R 770 /var/lib/joyzl/web-server
chmod -R 770 /var/log/joyzl/web-server

# 创建系统服务
mv /opt/joyzl/web-server/server.service /lib/systemd/system/joyzl-web-server.service
systemctl daemon-reload
systemctl enable joyzl-web-server.service

# 安装并配置低端口绑定
if command -v apt-get &> /dev/null; then
    apt-get update
    apt-get install -y authbind
elif command -v yum &> /dev/null; then
    yum install -y authbind
else
    echo "无法确定包管理器，未能安装 authbind"
fi
mkdir -p /etc/authbind/byport
touch /etc/authbind/byport/80
chown joyzl:joyzl /etc/authbind/byport/80
chmod 500 /etc/authbind/byport/80

# 设置防火墙规则
if command -v ufw &> /dev/null; then
	# UFW 防火墙
	ufw allow 1230/tcp comment "JOYZL SCADA Server ODBS"
	ufw allow 80/tcp comment "JOYZL SCADA Server HTTP"
fi
if command -v nft &> /dev/null && systemctl is-active nftables &> /dev/null; then
	# NFT 防火墙

    # 检查默认的 filter 表和 input 链是否存在
    if ! nft list table inet filter &> /dev/null; then
        nft add table inet filter
    fi
    if ! nft list chain inet filter input &> /dev/null; then
        nft add chain inet filter input '{ type filter hook input priority 0; policy accept; }'
    fi
    # 添加服务规则
    if ! nft list chain inet filter input | grep -q "dport 1230"; then
        nft add rule inet filter input tcp dport 1230 accept comment "JOYZL SCADA Server ODBS"
    fi
    if ! nft list chain inet filter input | grep -q "dport 80"; then
        nft add rule inet filter input tcp dport 80 accept comment "JOYZL SCADA Server HTTP"
    fi
fi


echo "安装完成"
echo "请在启动之前检查并配置数据目录"
echo "使用以下命令管理服务:"
echo "运行状态: systemctl status joyzl-web-server"
echo "启动服务: systemctl start joyzl-web-server"
echo "停止服务: systemctl stop joyzl-web-server"

