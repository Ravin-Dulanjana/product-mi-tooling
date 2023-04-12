package org.wso2.micro.integrator.dashboard.grpc.server;

import io.grpc.stub.StreamObserver;
import org.wso2.micro.integrator.grpc.proto.*;
import java.util.HashMap;

public class MIServiceImpl extends MIServiceGrpc.MIServiceImplBase{
    private static final HashMap<String, StreamObserver> clients = new HashMap<>();

    public static void requestHandler(String request){
        String[] command = request.toLowerCase().split(" ");
        String search = "";
        if (command.length != 1) {
            search = command[1];
        }
        if (command[0].equals("apis")){
            DataResponse dataResponse = DataResponse.newBuilder()
                    .setResponseType(DataResponse.ResponseType.APILIST)
                    .setResponse(search).build();
            for (StreamObserver client: clients.values()){
                client.onNext(dataResponse);
            }
        } else if (command[0].equals("endpoints")){
            DataResponse dataResponse = DataResponse.newBuilder()
                    .setResponseType(DataResponse.ResponseType.ENDPOINTLIST)
                    .setResponse(search).build();
            for (StreamObserver client: clients.values()){
                client.onNext(dataResponse);
            }
        }
        else if (command[0].equals("serverinfo")) {
            DataResponse dataResponse = DataResponse.newBuilder()
                    .setResponseType(DataResponse.ResponseType.SERVER)
                    .setResponse(search).build();
            for (StreamObserver client: clients.values()){
                client.onNext(dataResponse);
            }
        } else {
            System.out.println("Invalid data requested!");
        }
    }

    public StreamObserver<DataRequest> dataExchange(StreamObserver<DataResponse> responseObserver){
        return new StreamObserver<DataRequest>() {
            String nodeID;
            @Override
            public void onNext(DataRequest dataRequest) {
                if (dataRequest.hasHandshake()){
                    nodeID = dataRequest.getHandshake().getNodeID();
                    clients.put(nodeID, responseObserver);
                    System.out.println("Client connected");
                    System.out.println("Node ID: "+ nodeID);
                    System.out.println("Group ID: " + dataRequest.getHandshake().getGroupID() + "\n");
                } else {
                    System.out.println("Node id is "+ nodeID);
                    if (dataRequest.hasApiList()) {
                        System.out.println("API count: " + dataRequest.getApiList().getCount());
                        for (APISummary apiSummary : dataRequest.getApiList().getApiSummariesList()) {
                            System.out.println("__API Summary__ \n" + apiSummary + "\n");
                            DataResponse dataResponse = DataResponse.newBuilder()
                                    .setResponseType(DataResponse.ResponseType.API)
                                    .setResponse(apiSummary.getName()).build();
                            responseObserver.onNext(dataResponse);
                        }
                    } else if (dataRequest.hasApi()) {
                        System.out.println("__API__ \n" + dataRequest.getApi() + "\n");
                    } else if (dataRequest.hasEndpointList()) {
                        System.out.println("Endpoint count: " + dataRequest.getEndpointList().getCount());
                        for (EndpointSummary endpointSummary : dataRequest.getEndpointList().getEndPointSummariesList()) {
                            System.out.println("__Endpoint Summary__ \n" + endpointSummary + "\n");
                            DataResponse dataResponse = DataResponse.newBuilder()
                                    .setResponseType(DataResponse.ResponseType.ENDPOINT)
                                    .setResponse(endpointSummary.getName()).build();
                            responseObserver.onNext(dataResponse);
                        }
                    } else if (dataRequest.hasEndpoint()) {
                        System.out.println("__Endpoint__ \n" + dataRequest.getEndpoint() + "\n");
                    } else if (dataRequest.hasServerInfo()) {
                        System.out.println("__Server Info__ \n" + dataRequest.getServerInfo() + "\n");
                    } else {
                        System.out.println("Error");
                    }
                }
            }

            @Override
            public void onError(Throwable t) {
                clients.remove(responseObserver); // check
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                clients.remove(responseObserver);   //check
                responseObserver.onCompleted();
            }
        };
    }
}
