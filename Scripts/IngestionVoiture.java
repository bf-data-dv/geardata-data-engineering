import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class IngestionVoiture {
    public static void main(String[] args) {
        // 1. Paramètres de connexion
        String csvFile = "Data/data_voiture.csv"; // Vérifie bien le nom de ton fichier !
        String url = "jdbc:postgresql://localhost:5432/db_voiture";
        String user = "postgres";
        String password = System.getenv("DB_PASSWORD"); // "Votre mot de passe"

        // Requête SQL vers ton nouveau schéma
        String sql = "INSERT INTO staging.car_mats_raw (marque_voiture, id_voiture, modele_voiture, constructor_code, year_start, year_end, car_type, car_code, avts, pont, firstrow, secondrow, carboot) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             BufferedReader br = new BufferedReader(new FileReader(csvFile));
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false); // Pour booster la vitesse d'insertion
            String line;
            br.readLine(); // On ignore la première ligne (les titres)

            int count = 0;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(";"); // Séparateur point-virgule

                if (data.length >= 13) {
                    for (int i = 0; i < 13; i++) {
                        pstmt.setString(i + 1, data[i].trim());
                    }
                    pstmt.addBatch();
                    count++;
                }
                
                // On envoie par paquets de 500 pour ne pas saturer la mémoire
                if (count % 500 == 0) pstmt.executeBatch(); 
            }

            pstmt.executeBatch(); // On envoie le reste
            conn.commit();
            System.out.println("🚀 Succès ! " + count + " lignes insérées dans db_voiture.");

        } catch (Exception e) {
            System.err.println("❌ Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }
}