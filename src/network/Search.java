package network;

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;


public class Search {
	private LocalDateTime time;
	private String carNum;
	private String result;
	
	public Search(LocalDateTime time, String carNum) {
        this.time = time;
        this.carNum = carNum;
    }
	
	public String processOut() {
		try (Socket socket = new Socket(NetworkSettings.srvIpAddr, NetworkSettings.portNum);
	             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
	             DataInputStream in = new DataInputStream(socket.getInputStream())) {
			
			String flag = "33";
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/HH/mm");
            String formattedTime = time.format(formatter);
			
	            // Header 생성
	        	byte[] header = new byte[8];
	            ByteBuffer.wrap(header, 0, 4).put(flag.getBytes());
	            int bodySize = 26; // 22 + 4 
	            ByteBuffer.wrap(header, 4, 4).putInt(bodySize);

	            // Body 생성
	            byte[] body = new byte[bodySize];
	            ByteBuffer.wrap(body, 0, 22).put(formattedTime.getBytes());
	            ByteBuffer.wrap(body, 22, 4).put(carNum.getBytes());

	            // 데이터 전송
	            byte[] request = new byte[bodySize + 8];
	            System.arraycopy(header, 0, request, 0, 8);
	            System.arraycopy(body, 0, request, 8, bodySize);
	            out.write(request);

	            // 서버 응답 받기
	            byte[] response = new byte[25];
	            in.readFully(response);

	            // 응답 데이터 처리
	            String responseFlag = new String(Arrays.copyOfRange(response, 0, 4));
	            boolean isSuccess = response[4] == 1;
	            int responseBodySize = ByteBuffer.wrap(response, 5, 4).getInt();

	            // Body 데이터 처리
	            int price = ByteBuffer.wrap(response, 0, 4).getInt();
	            int usetime = ByteBuffer.wrap(response, 4, 4).getInt();
	            int parkspace = ByteBuffer.wrap(response, 8, 4).getInt();
	            int floor = ByteBuffer.wrap(response, 12, 4).getInt();
	            // 결과 문자열 생성
	            result = price + "/" + carNum;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
}

