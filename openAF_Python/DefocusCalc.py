# -*- coding: utf-8 -*-
"""
Created on Thu Aug  3 16:51:03 2023

@author: Jonathan Lightley

Copyright 2023 Imperial College London
Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice, this 
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
CONTRIBUTORS “AS IS” AND ANY EXPRESS OR IMPLIED WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR 
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
"""

import socket
import time
from DefocusCalcFunctions import autofocus
import os
os.environ["KMP_DUPLICATE_LIB_OK"]="TRUE"
af = autofocus()
HOST = "localhost"
PORT = 9999
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
print('Socket created')
try:
    s.bind((HOST, PORT))
except socket.error as err:
    print('Bind failed. Error Code : ' .format(err))
s.listen(10)
conn, addr = s.accept()
print("Socket Listening", addr)


def getZposition(i):
    if (i < 41):
        i = i+1
    else:
        i = -40
    time.sleep(0.1)    
    return i
        
def socketListener():
    st = -40
    back_st = 0
    boole =True
    last_t = time_ns = time.monotonic_ns()
    diff = 0
    while(boole):
        data = conn.recv(1024)
        msg = data.decode(encoding='UTF-8')
        if ( msg == "call"+"\r\n"):
            st = af.main()
            while (diff<200000000):
                time_now = time.monotonic_ns()
                diff = time_now-last_t
            last_t = time.monotonic_ns()
            diff = 0
            stt = str(st[0])+","+str(st[1])+","+str(st[2])+"\r\n"
            byt = stt.encode()
            conn.send(byt)
            print(str(st))
        elif (msg == "background_call" +"\r\n"):
            back_st += 1
            af.set_background(False)
            stt = str(back_st)+"\r\n"
            byt = stt.encode()
            conn.send(byt)
        elif (msg == "background_first_call" +"\r\n"):
            back_st += 1
            af.set_background(True)
            stt = str(back_st)+"\r\n"
            byt = stt.encode()
            conn.send(byt)
        elif (msg == "noise_background_call" +"\r\n"):
            back_st += 1
            af.set_noise_background()
            stt = str(back_st)+"\r\n"
            byt = stt.encode()
            conn.send(byt)
        elif ( msg == "deinit"+"\r\n"):
            st = getZposition(st)
            stt = str(st)+"\r\n"
            byt = stt.encode()
            conn.send(byt)
            boole == False
            break


            
        else:
            st = getZposition(st)
            stt = str(st)+"\r\n"
            byt = stt.encode()
            conn.send(byt)
            print("Message sent and not identified: " + str(st))
            
socketListener()
s.close()
print("Socket Closed")

if(False):
    while(True):                                          
        data = conn.recv(1024)
        print("call received in Python")
        msg = data.decode(encoding='UTF-8')
        print(msg)
        if ( msg == "call"+"\r\n"):
            st1 = 'Message'
            st2 = '\r\n'
            st = st1+st2
            byt = st.encode()
            conn.send(byt)
            print("Message sent form Python")
        else:
            
            print("Go away")
