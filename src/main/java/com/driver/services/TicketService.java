package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db

        Optional<Train> optionalTrain= trainRepository.findById(bookTicketEntryDto.getTrainId());
        Train train = optionalTrain.get(); // check train Id is exist or not
         if(optionalTrain==null){
             throw new Exception("Ivalid Train Id");
         }

          List<Integer> passengerIdList = bookTicketEntryDto.getPassengerIds();
          List<Passenger> passengers = new ArrayList<>();   // check passengerId is exist or not
        for(Integer id: passengerIdList){
           Passenger passenger = passengerRepository.findById(id).get();
          if(passenger==null){
              throw new Exception("Passenger Does Not Exist");
          }
              passengers.add(passenger);
         }

        String str = train.getRoute();
        String[] route = str.substring(1,str.length()-1).split(",");// check requested train route is valid or not

        boolean flag1 = false, flag2 = false;
        for (String s: route){
            if(s.equals(bookTicketEntryDto.getFromStation().toString())){
               flag1 = true;;
            }
            if(s.equals(bookTicketEntryDto.getToStation().toString())){
                flag2 = true;
            }
        }
        if(flag1==false || flag2==false){
            throw new Exception("Invalid stations");
        }

          // check required seats available or not
        if(train.getNoOfSeats()<bookTicketEntryDto.getNoOfSeats()){
            throw new Exception("Less tickets are available");
        }


        int fromStation = bookTicketEntryDto.getFromStation().ordinal(); // calculate total train fare
        int toStation = bookTicketEntryDto.getToStation().ordinal();
        int totalFare = (toStation-fromStation)*300* passengerIdList.size();

        Ticket ticket = new Ticket();
        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());
        ticket.setPassengersList(passengers);
        ticket.setTrain(train);
        ticket.setTotalFare(totalFare);
        Ticket savedTicket = ticketRepository.save(ticket);

        train.getBookedTickets().add(savedTicket);
        train.setNoOfSeats(train.getNoOfSeats()-passengers.size());
        trainRepository.save(train);

        Passenger passenger = passengerRepository.findById(bookTicketEntryDto.getBookingPersonId()).get();
        passenger.getBookedTickets().add(savedTicket);
        passengerRepository.save(passenger);

        return savedTicket.getTicketId();
    }
}
