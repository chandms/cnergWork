#!/usr/bin/python

from mininet.net import Mininet
from mininet.node import Controller, RemoteController, OVSController
from mininet.node import CPULimitedHost, Host, Node
from mininet.node import OVSKernelSwitch, UserSwitch
from mininet.node import IVSSwitch
from mininet.cli import CLI
from mininet.log import setLogLevel, info
from mininet.link import TCLink, Intf
from subprocess import call
import sys, os, stat
from mininet.term import runX11, makeTerm
import time

def writeExe(fname, content):
    fp = open(fname, "w")
    fp.write(content)
    fp.close()
    st = os.stat(fname)
    os.chmod(fname, st.st_mode | stat.S_IEXEC)

def myNetwork():

    net = Mininet( topo=None,
                   build=False)

    info( '*** Adding controller\n' )
    info( '*** Add switches\n')
    s1 = net.addSwitch('s1', cls=OVSKernelSwitch, failMode='standalone')
    s2 = net.addSwitch('s2', cls=OVSKernelSwitch, failMode='standalone')
    s3 = net.addSwitch('s3', cls=OVSKernelSwitch, failMode='standalone')

    info( '*** Add hosts\n')
    h1 = net.addHost('h1', cls=Host)
    h2 = net.addHost('h2', cls=Host)

    info( '*** Add links\n')
    linkProp1 = {'bw':1,'delay':'100ms'}
    linkProp2 = {'bw':2,'delay':'100ms'}
    linkProp3 = {'bw':3,'delay':'100ms'}
    net.addLink(h1, s1, cls=TCLink, **linkProp1)
    net.addLink(h1, s2, cls=TCLink, **linkProp2)
    net.addLink(h1, s3, cls=TCLink, **linkProp3)

    net.addLink(h2, s1, cls=TCLink, **linkProp1)
    net.addLink(h2, s2, cls=TCLink, **linkProp2)
    net.addLink(h2, s3, cls=TCLink, **linkProp3)

    info( '*** Starting network\n')
    net.build()
    info( '*** Starting controllers\n')
    for controller in net.controllers:
        controller.start()

    info( '*** Starting switches\n')
    net.get('s1').start([])
    net.get('s2').start([])
    net.get('s3').start([])

    info( '*** Post configure switches and hosts\n')
    
    h1.cmd("ifconfig h1-eth0 10.0.1.1 netmask 255.255.255.0")
    h1.cmd("ifconfig h1-eth1 192.168.2.1 netmask 255.255.255.0")
    h1.cmd("ifconfig h1-eth2 172.16.3.1 netmask 255.255.255.0")
    h2.cmd("ifconfig h2-eth0 10.0.1.2 netmask 255.255.255.0")
    h2.cmd("ifconfig h2-eth1 192.168.2.2 netmask 255.255.255.0")
    h2.cmd("ifconfig h2-eth2 172.16.3.2 netmask 255.255.255.0")

    # CLI(net)
#     waitTime = 120
#     
#     host = "h1"
#     dtHdr = ""
#     dtBdy = ""
#     dtRes = ""
#     for intf in xrange(3):
#         dtHdr += "dt%d1=$(cat /sys/class/net/%s-eth%d/statistics/tx_bytes); \n"%(intf, host, intf)
#         dtBdy += "dt%d=$(cat /sys/class/net/%s-eth%d/statistics/tx_bytes); "%(intf, host, intf) \
#               + "dt%dr=$(echo $dt%d-$dt%d1|bc); dt%d1=$dt%d; \n" %(intf, intf, intf, intf, intf)
#         dtRes += "$dt%dr $dt%d "%(intf, intf)
#     getstatcmd = """
#     %s
#     sleep 1; 
#     tm0=$(date "+%%s"); 
#     for x in `seq %d`; 
#         do tm=$(date "+%%s"); 
#         tmr=$(echo $tm-$tm0|bc); 
#         %s
#         echo $tmr $tm %s; 
#         sleep 1; 
#     done | tee /tmp/dt.csv
#     """%(dtHdr, waitTime, dtBdy, dtRes)
#     # print getstatcmd

#     getstatcmd = "python ./dynamic_plot.py"
#     for intf in xrange(3):
#         getstatcmd += " %s-eth%d"%(host, intf) 
# 
#     server = "./goserver.sh 9876"
#     client = "./goclient.sh 10.0.1.2 9876"
#     primaryDown = "ifconfig h2-eth0 down"
#     primaryUp = "ifconfig h2-eth0 up"

    server = "iperf -s"
    client = "iperf -c 10.0.1.2 -t 200"

#     otherDown = "ifconfig h2-eth1 down; ifconfig h2-eth2 down"
#     otherUp = "ifconfig h2-eth1 up; ifconfig h2-eth2 up"
#     markAsBackup = "kill -USR1 `cat /tmp/tcpClient.pid`"
# 
#     writeExe("/tmp/getstat", getstatcmd)
# 
#     termSt, popenSt = runX11(h1, "xterm -e "+"/tmp/getstat")
#     time.sleep(3)
    termSr, popenSr = runX11(h2, "xterm -e "+server)
    termCl, popenCl = runX11(h1, "xterm -e "+client)

#     time.sleep(15)
#     h1.cmd(markAsBackup)
#     time.sleep(20)
#     h2.cmd(primaryDown)
#     time.sleep(20)
#     h2.cmd(primaryUp)
#     time.sleep(20)
#     h2.cmd(otherDown)
#     time.sleep(20)
#     h2.cmd(otherUp)
    
    time.sleep(25)
    popenSt.terminate()
    popenCl.terminate()
    popenSr.terminate()
    
    net.stop()

if __name__ == '__main__':
    setLogLevel( 'info' )
    myNetwork()

