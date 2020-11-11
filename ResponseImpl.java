import java.time.LocalDateTime;

public class ResponseImpl implements Response {
    @Override
    public String welcomeMessage() {
        return ("Hello, you are the host client \r\n");
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
