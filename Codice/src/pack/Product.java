package pack;

public class Product {

    private String key;
    private String password;

    Product(String Key, String Password) {
        this.key = Key;
        this.password = Password;
    }


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
