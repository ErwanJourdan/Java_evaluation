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
    private static List<Booking> bookingList = new ArrayList<>();

    public static void enrollUser(User user) {
        EntityManager entityManager = EntityManagerFactorySingleton
                .getInstance().createEntityManager();
        try {
            entityManager.getTransaction().begin();
            users.add(user);
            entityManager.persist(user);
            System.out.println(user + " à été ajouté");
            entityManager.getTransaction().commit();
        } finally {
            entityManager.close();
        }
    }

    public static void enrollDriver(Driver driver) {
        EntityManager entityManager = EntityManagerFactorySingleton
                .getInstance().createEntityManager();
        try {
            entityManager.getTransaction().begin();
            driver.setAvailable(true);
            entityManager.persist(driver);
            System.out.println(driver + " à été ajouté");
            entityManager.getTransaction().commit();
        } finally {
            entityManager.close();
        }
    }

    public static Booking bookOneDriver(User user) {
        EntityManager entityManager = EntityManagerFactorySingleton
                .getInstance().createEntityManager();

        Query queryListDriverFree = entityManager
                .createQuery("SELECT d FROM Driver d WHERE d.available=true");

        List<Driver> queryFreeFreeDriver = (List<Driver>) queryListDriverFree.getResultList();
        System.out.println("Nombre de chauffeurs disponible : " + queryFreeFreeDriver.size());

        if (queryFreeFreeDriver.size() > 0) {
            try {
                entityManager.getTransaction().begin();
                Query queryDriverFree = entityManager
                        .createQuery("SELECT d FROM Driver d WHERE d.available=true");

                Driver queryFreeDriver = (Driver) queryDriverFree.setMaxResults(1).getSingleResult();
                System.out.println(queryFreeDriver);
                Booking booking = new Booking(user);
                booking.setUser(user);
                booking.setDriver(queryFreeDriver);
                booking.setStartOfBooking(Instant.now());
                queryFreeDriver.setAvailable(false);
                System.out.println(booking + "Est lancée");
                entityManager.persist(booking);
                entityManager.getTransaction().commit();
                return booking;
            } finally {
                entityManager.close();
            }
        }
        return null;
    }

    public static Booking finishBooking(Booking booking) {
        EntityManager entityManager = EntityManagerFactorySingleton
                .getInstance().createEntityManager();
        try {
            entityManager.getTransaction().begin();

            Query queryBooking = entityManager
                    .createQuery("SELECT b FROM Booking b WHERE b.id = :id");
            queryBooking.setParameter("id", booking.getId());
            Booking booking1 = (Booking) queryBooking.getSingleResult();

            booking1.getDriver().setAvailable(true);
            booking1.setEndOfBooking(Instant.now());
            entityManager.getTransaction().commit();
            System.out.println(booking + " est terminée");
            return booking;
        } finally {
            entityManager.close();
        }
    }

    public static void evaluateDriver(Booking booking, Integer evaluationOfTheUser) {
        EntityManager entityManager = EntityManagerFactorySingleton
                .getInstance().createEntityManager();
        entityManager.getTransaction().begin();
        Query queryBooking = entityManager
                .createQuery("SELECT b FROM Booking b WHERE b.id = :id");
        queryBooking.setParameter("id", booking.getId());
        Booking booking1 = (Booking) queryBooking.getSingleResult();
        try {

            booking1.setEvaluation(evaluationOfTheUser);
            entityManager.persist(booking1);
            System.out.println(booking + " à été évalué à " + evaluationOfTheUser + " étoiles");
            entityManager.getTransaction().commit();
        } finally {
            entityManager.close();
        }
    }

    public static List<Booking> listDriverBookings(Driver driver) {
        EntityManager entityManager = EntityManagerFactorySingleton
                .getInstance().createEntityManager();
        entityManager.getTransaction().begin();

        Query queryBookingList = entityManager
                .createQuery("SELECT b FROM Booking b WHERE b.driver = :driver");
        queryBookingList.setParameter("driver", driver);

        List<Booking> bookingList = (List<Booking>) queryBookingList.getResultList();

        if (bookingList.size() > 0) {
            try {

                System.out.println("Liste des courses du conduteur  " + driver + " " + bookingList);
                entityManager.getTransaction().commit();
                return bookingList;
            } finally {
                entityManager.close();
            }
        }
        return null;
    }

    public static List<Booking> listUnfinishedBookings() {
        EntityManager entityManager = EntityManagerFactorySingleton
                .getInstance().createEntityManager();

        entityManager.getTransaction().begin();

        Query queryUnFinishedBookingList = entityManager
                .createQuery("SELECT b FROM Booking b WHERE b.endOfBooking IS NULL");

        List<Booking> UnFinishedBookingList = (List<Booking>) queryUnFinishedBookingList.getResultList();
        System.out.println(UnFinishedBookingList.size());

        if (UnFinishedBookingList.size() > 0) {
            try {
                System.out.println("Liste en course en cours : " + UnFinishedBookingList);
                entityManager.getTransaction().commit();
                return UnFinishedBookingList;
            } finally {
                entityManager.close();
            }
        } return null ;
    }

        public static Float meanScore (Driver driver){
            EntityManager entityManager = EntityManagerFactorySingleton
                    .getInstance().createEntityManager();
            entityManager.getTransaction().begin();

            Query queryAvgScoreOfFinishedBookingByDriver = entityManager
                    .createQuery("SELECT AVG(b.evaluation) FROM Booking b WHERE b.endOfBooking IS NOT NULL AND b" +
                            ".driver = :driver");
            queryAvgScoreOfFinishedBookingByDriver.setParameter("driver", driver);
            Double avgScore = (Double) queryAvgScoreOfFinishedBookingByDriver.getSingleResult();

            try {
                System.out.println("Le score moyen du " +driver+ " est de "+ avgScore+ " étoiles");
                return avgScore.floatValue();
            } finally {
                entityManager.close();
            }
        }
    }
