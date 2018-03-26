package ch.adv.ui.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Listens for incoming snapshot transmissions and routes it to the correct
 * module parser.
 *
 * @author mtrentini
 */
@Singleton
public class SocketServer extends Thread {

    private ADVFlowControl flowControl;
    private ServerSocket javaSocket;

    private int portNr;
    private static final int DEFAULT_PORT = 8765;

    private static final String THREAD_NAME = "SocketServer Thread";
    private static final Logger logger = LoggerFactory.getLogger(SocketServer
            .class);

    @Inject
    public SocketServer(ADVFlowControl flowControl) {
        super(THREAD_NAME);
        portNr = DEFAULT_PORT;
        this.flowControl = flowControl;
    }

    /**
     * Accepts socket connections, acknowledges incoming snapshots and routes
     * the data to the corresponding parser.
     * Receiving the 'END' tag results in accepting new socket connections.
     */
    @Override
    public void run() {
        try {
            javaSocket = new ServerSocket(portNr);
            logger.info("Server socket created on port {}", portNr);

            while (true) {

                Socket socket = javaSocket.accept();
                BufferedReader reader = new BufferedReader(new
                        InputStreamReader(socket.getInputStream(),
                        StandardCharsets.UTF_8));
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                        socket.getOutputStream(), StandardCharsets.UTF_8),
                        true);

                String sessionJSON;
                while ((sessionJSON = reader.readLine()) != null) {
                    if (sessionJSON.equals("END")) {
                        logger.info("End of session transmission");
                        break;
                    }
                    logger.debug("Snapshot received: " + sessionJSON);

                    writer.println("OK");

                    flowControl.process(sessionJSON);
                }

            }
        } catch (IOException e) {
            logger.error("Unable to read incoming transmissions", e);
        }
    }

    /**
     * Set alternative port on which SocketServer should listen.
     *
     * @param port the port number to listen on
     */
    public void setPort(int port) {
        if (portNr >= 1024 && portNr <= 65535) {
            portNr = port;
        } else {
            portNr = DEFAULT_PORT;
        }
    }
}