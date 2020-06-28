#!/bin/bash
SHELL_FOLDER=$(cd $(dirname $0);pwd)

LOG_ERROR() {
    content=${1}
    echo -e "\033[31m[ERROR] ${content}\033[0m"
}

LOG_INFO() {
    content=${1}
    echo -e "\033[32m[INFO] ${content}\033[0m"
}

set -e
sed_cmd="sed -i"

if [ "$(uname)" == "Darwin" ];then
    sed_cmd="sed -i .bkp"
fi
while getopts "v:f" option;do
    case $option in
    v) version="$OPTARG";;
    f) config="true";;
    esac
done

if [[ -z "${version}" ]];then
    version=$(curl -s https://api.github.com/repos/FISCO-BCOS/console/releases | grep "tag_name" | sort -u | tail -n 1 | cut -d \" -f 4 | sed "s/^[vV]//")
fi
package_name="console.tar.gz"
echo "Downloading console ${version}"
download_link=https://github.com/FISCO-BCOS/console/releases/download/v${version}/${package_name}

if [ $(curl -IL -o /dev/null -s -w %{http_code}  https://www.fisco.com.cn/cdn/console/releases/download/v${version}/${package_name}) == 200 ];then
    curl -LO ${download_link} --speed-time 30 --speed-limit 102400 -m 450 || {
        echo -e "\033[32m Download speed is too low, try https://www.fisco.com.cn/cdn/console/releases/download/v${version}/${package_name} \033[0m"
        curl -LO https://www.fisco.com.cn/cdn/console/releases/download/v${version}/${package_name}
    }
else
    curl -LO ${download_link}
fi
tar -zxf ${package_name} && cd console && chmod +x *.sh

if [[ -n "${config}" ]];then
    cp conf/applicationContext-sample.xml conf/applicationContext.xml
    cp ../sdk/* conf/
    channel_listen_port=$(cat "${SHELL_FOLDER}"/node*/config.ini | grep channel_listen_port | cut -d = -f 2 | head -n 1)
    channel_listen_ip=$(cat "${SHELL_FOLDER}"/node*/config.ini | grep channel_listen_ip | cut -d = -f 2 | head -n 1)
    ${sed_cmd} "s/127.0.0.1:20200/127.0.0.1:${channel_listen_port}/" conf/applicationContext.xml
    echo -e "\033[32m console configuration completed successfully. \033[0m"
fi
