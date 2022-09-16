/*
Copyright 2019 GEOSIRIS

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.geosiris.webstudio.servlet.db.user;

import com.geosiris.energyml.utils.Pair;
import com.geosiris.webstudio.property.UserDBProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class UserDBInfos {
    public static Logger logger = LogManager.getLogger(UserDBInfos.class);

	// User data base is : account (user_id, login, password, mail, usr_grp)
	public static final String DB_USER_ID = "user_id";
	public static final String DB_LOGIN = "login";
	public static final String DB_PWD = "password";
	public static final String DB_MAIL = "mail";
	public static final String DB_GRP = "usr_grp";

	private static UserDBProperties dbProp = new UserDBProperties();

	public static Connection connect() {
		String URL = "jdbc:postgresql://" + dbProp.getHost() + (dbProp.getPort() != null ? ":" + dbProp.getPort() : "")
				+ "/" + dbProp.getDbName();

		logger.info("connecting to url '" + URL + "'");

		String dbDriver = "org.postgresql.Driver";
		Connection conn = null;
		try {
			Class.forName(dbDriver);
			conn = DriverManager.getConnection(URL, dbProp.getLogin(), dbProp.getPassword());
			// logger.error("Connected to the PostgreSQL server successfully.");
		} catch (SQLException | ClassNotFoundException e2) {
			logger.error("2) ERR connexion to User database : for url " + URL);
			logger.error(e2.getMessage());
			dbProp = new UserDBProperties();
			logger.info("We tried to reload user-db properties, please retry the connexion :");
			logger.info(dbProp);
		}

		return conn;
	}

	public static Boolean searchUser(String login, String pwd) {
		try (Connection conn = connect();
				PreparedStatement userQuery = conn.prepareStatement("Select count(*) " + "from account " + "where "
						+ DB_LOGIN + "= ? and " + DB_PWD + " = ?" + ";")) {
			userQuery.setString(1, login);
			userQuery.setString(2, hashPassword(pwd));
			ResultSet rs = userQuery.executeQuery();

			while (rs.next()) {
				conn.close();
				return rs.getInt(1) > 0;
			}
			conn.close();
		} catch (SQLException e) {
			logger.info(e.getMessage());
		}
		return false;
	}

	public static Pair<String, String> logUser(String login, String pwd) {
		try (Connection conn = connect();) {
			PreparedStatement userQuery = conn.prepareStatement(
					"Select * " + "from account " + "where " + DB_LOGIN + "= ? and " + DB_PWD + " = ?" + ";");
			userQuery.setString(1, login);
			userQuery.setString(2, hashPassword(pwd));
			ResultSet rs = userQuery.executeQuery();

			while (rs.next()) {
				conn.close();
				return new Pair<String, String>(rs.getString(DB_LOGIN), rs.getString(DB_GRP));
			}
			conn.close();
		} catch (SQLException e) {
			logger.info(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public static String getUserAttribute(String login, String db_attribute) {
		try (Connection conn = connect();
				PreparedStatement userQuery = conn.prepareStatement(
						"Select " + db_attribute + " " + "from account " + "where " + DB_LOGIN + "= ?" + ";")) {
			userQuery.setString(1, login);
			ResultSet rs = userQuery.executeQuery();

			while (rs.next()) {
				conn.close();
				return rs.getString(db_attribute);
			}
			conn.close();
		} catch (SQLException e) {
			logger.info(e.getMessage());
		}
		return "";
	}

	public static boolean deleteUser(String[] logins) {
		boolean result = true;
		for (String userName : logins) {
			result = result && deleteUser(userName);
		}
		return result;
	}

	public static boolean deleteUser(String login) {
		try (Connection conn = connect();
				PreparedStatement userQuery = conn
						.prepareStatement("delete " + "from account " + "where " + DB_LOGIN + " = ?" + ";")) {
			userQuery.setString(1, login);
			int value = userQuery.executeUpdate();
			conn.close();
			return value == 1;
		} catch (SQLException e) {
			logger.info(e.getMessage());
		}
		return false;
	}

	public static List<String> listUsers() {
		List<String> userList = new ArrayList<>();
		try (Connection conn = connect();
				PreparedStatement userQuery = conn.prepareStatement("Select * from account;")) {
			ResultSet rs = userQuery.executeQuery();
			while (rs.next()) {
				userList.add(rs.getString(DB_LOGIN));
			}
			conn.close();
		} catch (SQLException e) {
			logger.info(e.getMessage());
		}
		return userList;
	}

	public static String listUsersInfos() {
		String userList = "[ ";
		try (Connection conn = connect();
				PreparedStatement userQuery = conn.prepareStatement("Select * from account;")) {
			ResultSet rs = userQuery.executeQuery();
			while (rs.next()) {
				userList += "{ ";
				userList += "\"" + DB_LOGIN + "\" : ";
				userList += "\"" + rs.getString(DB_LOGIN) + "\", ";
				userList += "\"" + DB_GRP + "\" : ";
				userList += "\"" + rs.getString(DB_GRP) + "\", ";
				userList += "\"" + DB_MAIL + "\" : ";
				userList += "\"" + rs.getString(DB_MAIL) + "\"";
				userList += " },";
			}
			userList = userList.substring(0, userList.length() - 1); // supprime la derniere virgule
			conn.close();
		} catch (SQLException e) {
			logger.info(e.getMessage());
		}
		userList += "]";
		return userList;
	}

	public static boolean updateUser(String login, String pwd, String newPwd, String mail, String grp) {
		if (logUser(login, pwd) != null) {
			if (newPwd != null && newPwd.replaceAll(" ", "").length() <= 0) {
				newPwd = null;
			}
			if (mail != null && mail.replaceAll(" ", "").length() <= 0) {
				mail = getUserAttribute(login, DB_MAIL);
			}
			if (grp == null || grp.replaceAll(" ", "").length() <= 0) {
				grp = getUserAttribute(login, DB_GRP);
			}
			try (Connection conn = connect();
					PreparedStatement q_updateUser = conn
							.prepareStatement("UPDATE account " + "SET " + DB_PWD + "=?,  " + DB_MAIL + "=?, " + DB_GRP
									+ "='" + grp + "' " + "WHERE " + DB_LOGIN + "=? and " + DB_PWD + "=?;")) {

				q_updateUser.setString(1, hashPassword(newPwd != null ? newPwd : pwd));
				q_updateUser.setString(2, mail);
				q_updateUser.setString(3, login);
				q_updateUser.setString(4, hashPassword(pwd));
				logger.error(q_updateUser.toString());
				q_updateUser.execute();
				conn.close();
				return true;
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return false;
	}

	public static boolean createUser(String login, String pwd, String mail) {
		return createUser(login, pwd, mail, "user");
	}

	public static boolean createUser(String login, String pwd, String mail, String group) {
		try (Connection conn = connect();
				PreparedStatement q_testUserExists = conn
						.prepareStatement("Select * from account where " + DB_LOGIN + "=? or " + DB_MAIL + "=?;")) {

			q_testUserExists.setString(1, login);
			q_testUserExists.setString(2, mail);
			ResultSet rs = q_testUserExists.executeQuery();
			if (rs.next()) {
				conn.close();
				return false;
			}
			conn.close();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		try (Connection conn = connect()) {
			PreparedStatement q_addUser = conn.prepareStatement("INSERT INTO account (" + DB_LOGIN + ", " + DB_PWD
					+ ", " + DB_MAIL + ", " + DB_GRP + ")\r\n" + "VALUES (?,?,?,'" + group + "');");
			q_addUser.setString(1, login);
			q_addUser.setString(2, hashPassword(pwd));
			q_addUser.setString(3, mail);
			q_addUser.execute();
			conn.close();
			// } catch (SQLException e) {
		} catch (SQLException e) {
			logger.info("ERR : " + e.getClass() + " --> " + e.getMessage());
			return false;
		}
		return true;
	}

	public static String hashPassword(String pwd) {
		byte[] salt = dbProp.getHashSalt().getBytes();
		KeySpec spec = new PBEKeySpec(pwd.toCharArray(), salt, 65536, 128);
		SecretKeyFactory f;
		byte[] hash = null;
		try {
			f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			hash = f.generateSecret(spec).getEncoded();
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			logger.error(e.getMessage(), e);
		}
		Base64.Encoder enc = Base64.getEncoder();
		// System.out.printf("salt: %s%n", enc.encodeToString(salt));
		// System.out.printf("hash: %s%n", enc.encodeToString(hash));
		return enc.encodeToString(hash);
	}
}
