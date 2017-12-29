package com.ss.testserial.JNI;

public class UartComm {
    static {
        System.loadLibrary("SerialAPI");
    }
    /*
    * 初始化串口 device为需要打开串口的名字，如/dev/ttyS1 /dev/ttyS3. RS485固定为/dev/ttyS3
    * 返回值： 所打开文件句柄
    * */
    public native int uartInit(String device);
    /*
    * 释放串口资源
    * fd: 需要释放资源串口的句柄，由uartInit()函数返回
    * */
    public native int uartDestroy(int fd);
    /*
    * 设置串口属性
    *  fd: 所设置串口的句柄，由uartInit()函数返回
    * baud: 波特率 值为2400/4800/9600/19200/38400/57600/115400
    * dataBits:数据位数  5/6/7/8
    * parity: 校验
    * 		0: 无校验
    * 		1: 奇校验
    * 		2:  偶校验
    * stopBits:停止位 1/2
    * 如setOPt(9600,8,0,1)为设置9600波特率， 8N1属性
    */
    public native int setOpt(int fd, int baud, int dataBits, int parity, int stopBits);

    /**
     * 把数据发送出去
     * fd: 需要发送数据串口的句柄， 由uartInit()函数返回
     * byteBuf:数据缓冲
     * length: 要发送数据的长度
     */
    public native int send(int fd, int[] val, int length);

    /**
     *读取串口数据
     *fd: 需要接收数据串口的句柄， 由uartInit()函数返回
     * byteBuf： 数据缓冲
     * length: 要读取的数据长度
     * return :
     */
    public native int recv(int fd, int[] val, int length);
    public native int recvWihtTimeOut(int fd, int[] val, int length, int timeOut);
    /*
    * 把485设置为读模式或者写模式
    * read  1 : 设置为读模式
    * 		   0 : 设置为写模式
    * */
    public native int setRS485WriteRead(int writeRead);


    public class Rs485 {
        /*
        * 初始化485设备
        * return  true : 初始化成功
        * 		   false : 初始化失败
        * */
        public native boolean rs485Init();

        /*
        * 关闭485设备
        * return
        * */
        public native void rs485Destroy();

        /*
        * 取得每个已连接的485从板的板地址
        * addrnum  想要获取的地址数量， 获取的地址值最大为maxAddr
        * maxAddr  最大的地址值
        * retInfo  板地址存放数组
        * return   获取到的地址数量，即连接的从板数量
        * */
        public native int rs485GetBoardAddress(int addrnum, int maxAddr, int[] retInfo);

        /*
        * 打开 指定从板的指定的门
        * boardID  从板板地址
        * doorID   门号, 1-24
        * retInfo　单片机返回的数据
        * return    0表示获取信息成功，-1代表失败
        * */
        public native int rs485OpenGrid(int boardID, int doorID, int[] retInfo);

        /*
        * 获取　指定从板的指定的门 的状态
        * boardID  从板板地址
        * doorID   门号 1-24  0代表全部
        * retInfo　单片机返回的数据
        * return
        * */
        public native int rs485GetDoorState(int boardID, int doorID, int[] retInfo);

        /*
         * 获取协议ID
         * boardID  锁控板卡地址
         * retInfo  返回板卡程序协议
         * return
         */
        public native int rs485GetProtocalID(int boardID, int[] retInfo);
    }
}
