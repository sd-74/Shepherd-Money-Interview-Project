package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.CreateUserPayload;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;


@RestController
public class UserController {

    // TODO: wire in the user repository (~ 1 line)
    @Autowired
    private UserRepository userRepository;


    @PutMapping("/user")
    public ResponseEntity<Integer> createUser(@RequestBody CreateUserPayload payload) {
        User user = new User();
        user.setName(payload.getName());
        user.setEmail(payload.getEmail());
        userRepository.save(user);

        // return the users id 
        return ResponseEntity.ok(user.getId());

    }

    @DeleteMapping("/user")
    public ResponseEntity<String> deleteUser(@RequestParam int userId) {
        // TODO: Return 200 OK if a user with the given ID exists, and the deletion is successful
        //       Return 400 Bad Request if a user with the ID does not exist
        //       The response body could be anything you consider appropriate
        Optional<User> optionalUser = userRepository.findById(userId);

        // return a 400 if a user w the id DNE
        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body("user w id doesnt exist");
        }
        userRepository.deleteById(userId);

        // return a ok response w the user id
        return ResponseEntity.ok("adding user " + userId + " was succesful");
    }
}
