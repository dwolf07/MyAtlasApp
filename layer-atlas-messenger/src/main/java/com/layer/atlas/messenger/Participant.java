package com.layer.atlas.messenger;

import com.layer.atlas.Atlas;

public class Participant implements Atlas.Participant {
    public String userId;
    public String firstName;
    public String lastName;

    public String getId() {
        return userId;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }
    
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Contact [userId: ").append(userId).append(", firstName: ").append(firstName).append(", lastName: ").append(lastName).append("]");
        return builder.toString();
    }
}
