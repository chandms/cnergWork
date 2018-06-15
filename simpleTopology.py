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
from mininet.term import runX11, makeTerm
import time

def myNetwork():

    net = Mininet( topo=None,
                   build=False,
                   ipBase='10.0.0.0/8')

    info( '*** Adding controller\n' )
    info( '*** Add switches\n')
    s2 = net.addSwitch('s2', cls=OVSKernelSwitch, failMode='standalone')
    s1 = net.addSwitch('s1', cls=OVSKernelSwitch, failMode='standalone')
    s3 = net.addSwitch('s3', cls=OVSKernelSwitch, failMode='standalone')
    s4 = net.addSwitch('s4', cls=OVSKernelSwitch, failMode='standalone')

    info( '*** Add hosts\n')
    h2 = net.addHost('h2', cls=Host, ip='10.0.0.2', defaultRoute=None)
    h6 = net.addHost('h6', cls=Host, ip='10.0.0.6', defaultRoute=None)
    h1 = net.addHost('h1', cls=Host, ip='10.0.0.1', defaultRoute=None)
    h4 = net.addHost('h4', cls=Host, ip='10.0.0.4', defaultRoute=None)
    h5 = net.addHost('h5', cls=Host, ip='10.0.0.5', defaultRoute=None)
    h3 = net.addHost('h3', cls=Host, ip='10.0.0.3', defaultRoute=None)

    info( '*** Add links\n')
    hs = {'bw':2,'delay':'50ms'}
    net.addLink(h1, s1, cls=TCLink , **hs)
    net.addLink(h2, s1, cls=TCLink , **hs)
    net.addLink(h3, s2, cls=TCLink , **hs)
    net.addLink(h4, s2, cls=TCLink , **hs)
    net.addLink(h5, s3, cls=TCLink , **hs)
    net.addLink(h6, s3, cls=TCLink , **hs)
    ss = {'bw':6,'delay':'10ms'}
    net.addLink(s1, s4, cls=TCLink , **ss)
    net.addLink(s2, s4, cls=TCLink , **ss)
    net.addLink(s3, s4, cls=TCLink , **ss)

    info( '*** Starting network\n')
    net.build()
    info( '*** Starting controllers\n')
    for controller in net.controllers:
        controller.start()

    info( '*** Starting switches\n')
    net.get('s2').start([])
    net.get('s1').start([])
    net.get('s3').start([])
    net.get('s4').start([])

    info( '*** Post configure switches and hosts\n')
    server = "./server.sh"
    client1 = "./client.sh /home/viscous/Documents/f1/ 10.0.0.6"
    client2 = "./client.sh /home/viscous/Documents/f2/ 10.0.0.6"
    client3 = "./client.sh /home/viscous/Documents/f3/ 10.0.0.6"
    client4 = "./client.sh /home/viscous/Documents/f4/ 10.0.0.6"
    client5 = "./client.sh /home/viscous/Documents/f5/ 10.0.0.6"


    termSr, popenSr = runX11(h6, "xterm -e "+server)
    termCl1, popenCl1 = runX11(h1, "xterm -e "+client1)
    termCl2, popenCl2 = runX11(h2, "xterm -e "+client2)
    termCl3, popenCl3 = runX11(h3, "xterm -e "+client3)
    termCl4, popenCl4 = runX11(h4, "xterm -e "+client4)
    termCl5, popenCl5 = runX11(h5, "xterm -e "+client5)


    popenCl1.wait()
    popenCl2.wait()
    popenCl3.wait()
    popenCl4.wait()
    popenCl5.wait()
    time.sleep(5)
    popenSr.terminate()

#     CLI(net)
    net.stop()

if __name__ == '__main__':
    setLogLevel( 'info' )
    myNetwork()

