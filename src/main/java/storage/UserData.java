package storage;

//Удобное хранение пользователей в удобном формате для JSON
public class UserData {
    private Long id;
    private String login;
    private String passwordHash;

    public UserData(){
    }

    public UserData(Long id, String login, String passwordHash){
        this.id = id;
        this.login = login;
        this.passwordHash = passwordHash;
    }

    public Long getId() {
        return id;
    }
     public String getLogin() {
        return login;
     }

     public String getPasswordHash() {
        return passwordHash;
     }
     public void setId(Long id) {
        this.id = id;
     }
     public void setLogin(String login) {
        this.login = login;
     }
     public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
     }

}
