import java.time.LocalDateTime;

public class ResponseImpl implements Response {
    @Override
    public String welcomeMessage(int clientNumber) {
        return ("Hello, you are client #"+ clientNumber + "\r\n");
    }

    @Override
    public String capitalizeString(String input) {
        return input.toUpperCase();
    }

    @Override
    public String timeString() {
        return (LocalDateTime.now() + "\r\n");
    }
}
