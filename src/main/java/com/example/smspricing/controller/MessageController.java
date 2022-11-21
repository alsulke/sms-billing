package com.example.smspricing.controller;

import com.example.smspricing.controller.model.SMSRequest;
import com.example.smspricing.entity.Customer;
import com.example.smspricing.entity.SubscriptionPlan;
import com.example.smspricing.repository.CustomerRepository;
import com.example.smspricing.repository.SubscriptionPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

@RestController
@RequestMapping("sms")
public class MessageController {

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    SubscriptionPlanRepository subscriptionPlanRepository;

    @PostMapping(value = "/send")
    public ResponseEntity sendMessage(@RequestBody SMSRequest smsRequest) {
        if (smsRequest != null) {
            countMessage(smsRequest);
            // send(smsRequest);
            return new ResponseEntity("Message sent successfully", HttpStatus.OK);

        }

        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("bill/{customerId}")
    public double billAmount(@PathVariable Long customerId) {
        Optional<Customer> customer = customerRepository.findById(customerId);
        if (customer.isPresent()) {
            long smsCount = customer.get().getMessagesCount();
            int subscriptionId = customer.get().getSubscriptionId();
            return calculateAmountForCurrentMonth(smsCount, subscriptionId);
        }
        return 0;
    }

    private void countMessage(SMSRequest smsRequest) {
        Optional<Customer> customer = customerRepository.findById(smsRequest.getCustomerId());
        if (customer.isPresent()) {
            if(isLastDayOfMonth()) {
                customer.get().setMessagesCount(0L);
            } else {
                customer.get().setMessagesCount(customer.get().getMessagesCount() + 1);
            }
            customerRepository.save(customer.get());
        }
    }

    private boolean isLastDayOfMonth() {
        Date today = new Date();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);

        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.DATE, -1);

        Date lastDayOfMonth = calendar.getTime();
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        return sdf.format(today).equals(sdf.format(lastDayOfMonth));
    }

    private double calculateAmountForCurrentMonth(long smsCount, int subscriptionId) {
        Optional<SubscriptionPlan> subscriptionPlan = subscriptionPlanRepository.findById(subscriptionId);
        if (subscriptionPlan.isPresent()) {
            if (isFreeMessage(smsCount, subscriptionPlan.get().getLimit())) {
                return 0;
            }
            return smsCount * subscriptionPlan.get().getCostPerMessage();
        }
        return 0;

    }

    private boolean isFreeMessage(long smsCount, int limit) {
        return smsCount <= limit;
    }


}
