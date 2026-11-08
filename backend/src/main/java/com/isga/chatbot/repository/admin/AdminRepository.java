package com.isga.chatbot.repository.admin;

import com.isga.chatbot.model.admin.Admin;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AdminRepository extends MongoRepository<Admin, String> {

    Admin findByEmail(String email);
}