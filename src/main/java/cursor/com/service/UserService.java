package cursor.com.service;

import cursor.com.entity.User;

public interface UserService {

    Long create(User user);
    User getByUsername(String username);

}
