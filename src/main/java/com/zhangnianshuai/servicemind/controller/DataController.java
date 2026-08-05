package com.zhangnianshuai.servicemind.controller;

import com.zhangnianshuai.servicemind.model.BookingDetails;
import com.zhangnianshuai.servicemind.service.BookingService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/booking")
public class DataController {

    private final BookingService bookingService;

    public DataController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    
    @GetMapping
    public List<BookingDetails> getAllBookings() {
        return bookingService.getBookings();
    }
}
