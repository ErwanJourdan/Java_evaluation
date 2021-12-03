package com.freestack.evaluation.api;

import com.freestack.evaluation.EntityManagerFactorySingleton;
import com.freestack.evaluation.models.Booking;
import com.freestack.evaluation.models.Driver;
import com.freestack.evaluation.models.User;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class UberApi {

    private static List<User> users = new ArrayList<>();
    private static List<Driver> driversAvailable = new ArrayList<>();
    private static List<Booking> bookingList = new ArrayList<>();

    public static void enrollUser(User user){
        EntityManager entityManager = EntityManagerFactorySingleton
                .getInstance().createEntityManager();
        try {
            entityManager.getTransaction().begin();
            users.add(user);
            entityManager.persist(user);
            System.out.println(user +" à été ajouté");
            entityManager.getTransaction().commit();
        } finally {
            entityManager.close();
        }
    }

    public static void enrollDriver(Driver driver){
        EntityManager entityManager = EntityManagerFactorySingleton
                .getInstance().createEntityManager();
        try {
            entityManager.getTransaction().begin();
            driver.setAvailable(true);
            driversAvailable.add(driver);
            entityManager.persist(driver);
            System.out.println(driver +" à été ajouté");
            entityManager.getTransaction().commit();
        } finally {
            entityManager.close();
        }
    }

    public static Booking bookOneDriver(User user){
        EntityManager entityManager = EntityManagerFactorySingleton
                .getInstance().createEntityManager();
        System.out.println(driversAvailable.size());
        if(driversAvailable.size()>0){
            try {
                entityManager.getTransaction().begin();
                Query queryDriverFree = entityManager
                        .createQuery("SELECT d FROM Driver d WHERE d.available=true");

                Driver queryFreeDriver =  (Driver) queryDriverFree.setMaxResults(1).getSingleResult();
                System.out.println(queryFreeDriver);
                Booking booking = new Booking(user);
                booking.setUser(user);
                booking.setDriver(queryFreeDriver);
                booking.setStartOfBooking(Instant.now());
                driversAvailable.remove(queryFreeDriver);
                queryFreeDriver.setAvailable(false);
                System.out.println(booking+ "Est lancée");
                entityManager.persist(booking);
                entityManager.getTransaction().commit();
                return booking;
                }
            finally {
                entityManager.close();
            }
        } return null;
    }

    public static Booking finishBooking(Booking booking){
        EntityManager entityManager = EntityManagerFactorySingleton
                .getInstance().createEntityManager();
        try {
            entityManager.getTransaction().begin();
            booking.getDriver().setAvailable(true);
            booking.setEndOfBooking(Instant.now());
            System.out.println(booking +" est terminée");
            entityManager.getTransaction().commit();
            return booking;
        } finally {
            entityManager.close();
        }
    }

    public static void evaluateDriver(Booking booking, Integer evaluationOfTheUser){
        EntityManager entityManager = EntityManagerFactorySingleton
                .getInstance().createEntityManager();
        try {
            entityManager.getTransaction().begin();
            booking.setEvaluation(evaluationOfTheUser);
            entityManager.persist(booking);
            System.out.println(booking +" à été évalué à "+ evaluationOfTheUser+ " étoiles");
            entityManager.getTransaction().commit();
        } finally {
            entityManager.close();
        }
    }

    public static List<Booking> listDriverBookings(Driver driver){
        EntityManager entityManager = EntityManagerFactorySingleton
            .getInstance().createEntityManager();
        try {
            entityManager.getTransaction().begin();

            Query queryBookingByDriver = entityManager
                    .createQuery("SELECT b FROM Booking b WHERE b.driver_id = :driver_id");
            queryBookingByDriver.setParameter("driver_id", driver);
            List result = queryBookingByDriver.getResultList();
            System.out.println("Liste des courses du conduteur  " + driver + result);
            entityManager.getTransaction().commit();
            return result;
        } finally {
            entityManager.close();
        }
    }

    public static List<Booking> listUnfinishedBookings(){
        EntityManager entityManager = EntityManagerFactorySingleton
                .getInstance().createEntityManager();
        try {
            entityManager.getTransaction().begin();

            Query querylistUnfinishedBookings = entityManager
                    .createQuery("SELECT b FROM Booking b WHERE b.end_of_booking = :end_of_booking");
            querylistUnfinishedBookings.setParameter("end_of_booking", null);
            List result = querylistUnfinishedBookings.getResultList();
            System.out.println("Liste en course en cours : " + result);
            entityManager.getTransaction().commit();
            return result;
        } finally {
            entityManager.close();
        }
    }

    public static Float meanScore(Driver driver){
        EntityManager entityManager = EntityManagerFactorySingleton
                .getInstance().createEntityManager();
        try {
            entityManager.getTransaction().begin();

            Query queryNmberOfFinishedBookinks =  entityManager.createQuery("SELECT SUM(b) FROM Booking b " +
                    "WHERE" +
                    " b.driver_id = :driver_id");
            Float NmberOfFinishedBookinks = (Float) queryNmberOfFinishedBookinks.getSingleResult();


            Query querySumOfEvaluations = entityManager
                    .createQuery("SELECT SUM(b.Evaluation) FROM Booking b WHERE b.driver_id = :driver_id");
            querySumOfEvaluations.setParameter("driver_id", driver);
            Float SumOfEvaluations  = (Float) querySumOfEvaluations.getSingleResult();

            System.out.println("Moyenne du conduteur  " + driver + SumOfEvaluations/NmberOfFinishedBookinks + " étoiles");
            entityManager.getTransaction().commit();
            Float result = SumOfEvaluations/NmberOfFinishedBookinks;
            return result;
        } finally {
            entityManager.close();
        }

    }

}
