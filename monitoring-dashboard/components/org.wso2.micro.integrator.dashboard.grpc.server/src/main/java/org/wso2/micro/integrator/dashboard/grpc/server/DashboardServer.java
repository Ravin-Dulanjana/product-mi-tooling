package org.wso2.micro.integrator.dashboard.grpc.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.util.Scanner;

public class DashboardServer {
    public static void start() throws Exception {
        Server server = ServerBuilder.forPort(8080).addService(new MIServiceImpl()).build();
        server.start();
        System.out.println("Server started...\n");
        while(true) {
            Scanner scanner = new Scanner(System.in);
            String cmd = scanner.nextLine();
            MIServiceImpl.requestHandler(cmd);

        }
    }
}

