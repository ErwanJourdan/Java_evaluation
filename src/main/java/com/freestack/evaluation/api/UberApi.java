package com.freestack.evaluation.api;

import com.freestack.evaluation.EntityManagerFactorySingleton;
import com.freestack.evaluation.models.Booking;
import com.freestack.evaluation.models.Driver;
import com.freestack.evaluation.models.User;

import javax.persistence.EntityManager;
import javax.persistence.Query;
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

        // retourner la liste des drivers pour n en avoir que le nombre est dommage, hibernate va instancier n objets (ce qui peut être vraiment beaucoup sur une DB de production) alors qu'on ne les utilise pas
        Integer freeDriverCount = (List<Driver>) queryListDriverFree.getMaxResults();
        System.out.println("Nombre de chauffeurs disponible : " + freeDriverCount);

        if (freeDriverCount > 0) {
            try {
                entityManager.getTransaction().begin();
                Query queryDriverFree = entityManager
                        .createQuery("SELECT d FROM Driver d WHERE d.available=true");
                //setMaxResults(1) est pertinent
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
            System.out.println(booking + " à été évalué à " + evaluationOfTheUser + " étoiles");
            entityManager.getTransaction().commit();
        } finally {
            entityManager.close();
        }
    }

    public static List<Booking> listDriverBookings(Driver driver) {
        EntityManager entityManager = EntityManagerFactorySingleton
                .getInstance().createEntityManager();
        //on a pas besoin de transaction en lecture seule

        Query queryBookingList = entityManager
                .createQuery("SELECT b FROM Booking b WHERE b.driver = :driver");
        queryBookingList.setParameter("driver", driver);

        List<Booking> bookingList = (List<Booking>) queryBookingList.getResultList();
        // on evite les doublons de code
        try {
            if (bookingList.size() > 0) {
                System.out.println("Liste des courses du conduteur  " + driver + " " + bookingList);
                return bookingList;
            }
            return null;
        } finally {
            entityManager.close();
        }
    }

    //meme commentaire qu au dessus , doublons de code et transactions
    public static List<Booking> listUnfinishedBookings() {
        EntityManager entityManager = EntityManagerFactorySingleton
                .getInstance().createEntityManager();

        entityManager.getTransaction().begin();

        Query queryUnFinishedBookingList = entityManager
                .createQuery("SELECT b FROM Booking b WHERE b.endOfBooking IS NULL");

        List<Booking> UnFinishedBookingList = (List<Booking>) queryUnFinishedBookingList.getResultList();

        if (UnFinishedBookingList.size() > 0) {
            try {
                System.out.println("Liste en course en cours : " + UnFinishedBookingList);
                entityManager.getTransaction().commit();
                return UnFinishedBookingList;
            } finally {
                entityManager.close();
            }
        }
        entityManager.close();
        return null;
    }

    public static Float meanScore(Driver driver) {
        EntityManager entityManager = EntityManagerFactorySingleton
                .getInstance().createEntityManager();
        
        //cela apporte de la confusion, on peut légitimement supposer que si il y a une note, c est une évaluation à prendre en compte 
        //et que elle n'est pas à conidérer seulement si il y a la présence de la date de fin, cela simplifie le code
        /*entityManager.getTransaction().begin();
        Query queryOfFinishedBookingByDriver = entityManager
                .createQuery("SELECT b FROM Booking b WHERE b.endOfBooking IS NOT NULL AND b" +
                        ".driver = :driver");
        queryOfFinishedBookingByDriver.setParameter("driver", driver);
        List<Booking> FinishedBookingByDriver =
                (List<Booking>) queryOfFinishedBookingByDriver.getResultList();

        if (FinishedBookingByDriver.size() > 0) {

            Query queryAvgScoreOfFinishedBookingByDriver = entityManager
                    .createQuery("SELECT AVG(b.evaluation) FROM Booking b WHERE b.endOfBooking IS NOT NULL AND b" +
                            ".driver = :driver");
            queryAvgScoreOfFinishedBookingByDriver.setParameter("driver", driver);
            Double avgScore = (Double) queryAvgScoreOfFinishedBookingByDriver.getSingleResult();
            try {
                System.out.println("Le score moyen du " + driver + " est de " + avgScore + " étoiles");
                return avgScore.floatValue();
            } finally {
                entityManager.close();
            }
        }
        entityManager.close();
        return null;
        */
        Query query = em.createQuery("SELECT AVG(b.evaluation) FROM Booking b WHERE b.uberDriver= :uberD");
        query.setParameter("uberD", uberDriver);
        Double result = (Double) query.getSingleResult();
        float newResult = result.floatValue();
        return newResult;
    }
}
