package com.shepherdmoney.interviewproject.controller;


import com.shepherdmoney.interviewproject.model.CreditCard;
import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.CreditCardRepository;
import com.shepherdmoney.interviewproject.model.BalanceHistory;

import com.shepherdmoney.interviewproject.repository.UserRepository;

import org.springframework.http.HttpStatus;

import com.shepherdmoney.interviewproject.vo.request.AddCreditCardToUserPayload;
import com.shepherdmoney.interviewproject.vo.request.UpdateBalancePayload;
import com.shepherdmoney.interviewproject.vo.response.CreditCardView;
import com.shepherdmoney.interviewproject.repository.CreditCardRepository;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;




@RestController
public class CreditCardController {

    // TODO: wire in CreditCard repository here (~1 line)
    @Autowired
    CreditCardRepository creditCardRepository;

    @Autowired
    UserRepository userRepository;


    @PostMapping("/credit-card")
    public ResponseEntity<Integer> addCreditCardToUser(@RequestBody AddCreditCardToUserPayload payload) {
        // TODO: Create a( credit card entity, and then associate that credit card with user with given userId
        //       Return 200 OK with the credit card id if the user exists and credit card is successfully associated with the user
        //       Return other appropriate response code for other exception cases
        //       Do not worry about validating the card number, assume card number could be any arbitrary format and length
        
        // create a user 
        Optional<User> optionalUser = userRepository.findById(payload.getUserId());
        // return a 404 if user DNE
        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // create a new cc for user
        CreditCard creditCard = new CreditCard();
        User user = optionalUser.get();
        creditCard.setOwner(user);
        creditCard.setNumber(payload.getCardNumber());


        creditCardRepository.save(creditCard);
        // return 200 if everything is fine
        return ResponseEntity.ok(creditCard.getId());

        
    }

    @GetMapping("/credit-card:all")
    public ResponseEntity<List<CreditCardView>> getAllCardOfUser(@RequestParam int userId) {
        // create a user 
        Optional<User> optionalUser = userRepository.findById(userId);
        // return a 404 if user DNE
        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User user = optionalUser.get();
        List<CreditCard> creditCards = user.getCreditCards();    
    
        // Convert CreditCard objects to CreditCardView objects
        List<CreditCardView> creditCardViews = creditCards.stream()
            .map(creditCard -> {
                CreditCardView view = new CreditCardView(creditCard.getIssuanceBank(), creditCard.getNumber());
                // Populate CreditCardView attributes from CreditCard
                // view.setId(creditCard.getId());
                // view.setIssuanceBank();
                // view.setNumber();
                // view.setOwner(creditCard.getOwner());
                return view;
            })
            .collect(Collectors.toList());
    
    // return the list of CreditCardView objects
    return ResponseEntity.ok(creditCardViews);
    }

    @GetMapping("/credit-card:user-id")
    public ResponseEntity<Integer> getUserIdForCreditCard(@RequestParam String creditCardNumber) {
        
        //create a creditCard object using the number passed in 
        Optional<CreditCard> creditCard = creditCardRepository.findByNumber(creditCardNumber);
        // check if there is a valid creditcard with that number
        if (creditCard.isPresent()) {
            return ResponseEntity.ok(creditCard.get().getOwner().getId());
        }
        // return a 400 bad request if there is no user with that cc number
        return ResponseEntity.badRequest().build();

    }

    @PostMapping("/credit-card:update-balance")
    public ResponseEntity<Void> updateBalances(@RequestBody UpdateBalancePayload[] payload) {
        //TODO: Given a list of transactions, update credit cards' balance history.
        //      1. For the balance history in the credit card
        //      2. If there are gaps between two balance dates, fill the empty date with the balance of the previous date
        //      3. Given the payload `payload`, calculate the balance different between the payload and the actual balance stored in the database
        //      4. If the different is not 0, update all the following budget with the difference
        //      For example: if today is 4/12, a credit card's balanceHistory is [{date: 4/12, balance: 110}, {date: 4/10, balance: 100}],
        //      Given a balance amount of {date: 4/11, amount: 110}, the new balanceHistory is
        //      [{date: 4/12, balance: 120}, {date: 4/11, balance: 110}, {date: 4/10, balance: 100}]
        //      Return 200 OK if update is done and successful, 400 Bad Request if the given card number
        //        is not associated with a card.


        // sort the given array by date
        Arrays.sort(payload, Comparator.comparing(UpdateBalancePayload::getBalanceDate));
        for (UpdateBalancePayload load : payload) {
            Optional<CreditCard> optionalCreditCard = creditCardRepository.findByNumber(load.getCreditCardNumber());

            // if a user with that cc # DNE, return a 400
            if (optionalCreditCard.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            CreditCard creditCard = optionalCreditCard.get();
            List<BalanceHistory> balanceHistories = creditCard.getBalanceHistory();
            // create a balance history with the data passed in 
            Optional<BalanceHistory> currentBalance = balanceHistories.stream().filter(history -> history.getDate().equals(load.getBalanceDate())).findFirst();

            // if it DNE in the list, add a new one with it
            if (currentBalance.isEmpty()) {
                balanceHistories.add(new BalanceHistory(load.getBalanceDate(), load.getBalanceAmount(), creditCard));
            } else {    //otherwise, update the current one with the data in currentBalance
                currentBalance.get().setBalance(currentBalance.get().getBalance() + load.getBalanceAmount());
            }
            creditCardRepository.save(creditCard);
        }
        return ResponseEntity.ok().build();  
    }
    
}
