package com.example.sql;

import java.sql.*;
import org.json.*;

public class SqlDatabase {
    public static Connection c = null;
    static Statement stmt = null;
    //"INSERT INTO ORDERS(orderDate) VALUES(?)"
    //"INSERT INTO ITEMS(ORDERID, ITEMNAME, ITEMUNITPRICE, ITEMQUANTITY) VALUES((SELECT seq from sqlite_sequence where name="orders")?,?,?)"
    public static void main(String args[]) throws SQLException{
    }

    public static void openConnection(){
        try{
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:test.db");
            c.setAutoCommit(false);
            System.out.println("Connection Successful");
            stmt = c.createStatement();
        }
        catch (Exception e){
            System.out.println(e.getClass());
            System.out.println("Connection Unsuccessful");
        }
    }

    public static void createTables(){
        try{
            openConnection();
            String createOrderTableQuery = "create table orders( orderId integer primary key AutoIncrement, orderDate date default current_date, orderStatus varchar2 default \"New\", Collection_Of_Items varchar2)";
            String createItemTableQuery = "create table Items(itemId integer primary key AutoIncrement, orderId varchar2 REFERENCES Orders(orderId), itemName varchar2, itemUnitPrice varchar2, itemQuantity varchar2)";
            stmt.executeUpdate(createOrderTableQuery);
            stmt.executeUpdate(createItemTableQuery);
            System.out.println("Tables Created");
        }
        catch(SQLException e){
            if(e.getMessage().indexOf("already exists")>0){
                System.out.println("Tables Created");
            }
            else{
                System.out.println("Failed to create Tables");
            }
        }
        finally{
            try{
                c.commit();
            }
            catch(Exception e){
                System.out.println(e.getClass());
            }
            closeConnection();                
        }
    }

    public static String createOrders(JSONArray jorders){
        JSONArray resultArray = new JSONArray();
        try{
            openConnection();
            for(var i = 0; i<jorders.length(); i++){
                JSONObject orderitem = jorders.getJSONObject(i);
                String orderDate = orderitem.getString("orderDate");
                JSONArray items = orderitem.getJSONArray("items");
                createOrder(orderDate);
                ResultSet rset = stmt.executeQuery("SELECT SEQ FROM SQLITE_SEQUENCE");
                int orderId = rset.getInt("SEQ");
                for(var j = 0; j<items.length(); j++){
                    JSONObject curritem = items.getJSONObject(j);
                    String itemName = curritem.getString("itemName");
                    String itemUnitPrice = curritem.getString("itemUnitPrice");
                    String itemQuantity = Integer.toString(curritem.getInt("itemQuantity"));
                    try{
                        createItems(itemName, itemUnitPrice, itemQuantity);
                    }
                    catch(SQLException e){
                        c.rollback();
                        resultArray.put("Order not created.");
                    }
                }
                resultArray.put("Order "+orderId+" created");
            }
        }
        catch(SQLException e){
            e.printStackTrace();
            System.out.println("Failed to create Order");
            resultArray.put("Failed to create Order");
            try{
                c.rollback();
            }
            catch(SQLException ex){
                System.out.println(ex);
            }
        }
        catch(Exception e){
            System.out.println(e.getClass());
            try{
                c.rollback();
            }
            catch(SQLException ex){
                System.out.println(ex);
            }
        }
        finally{
            try{
                c.commit();
            }
            catch(SQLException e){
                System.out.println("Failed to create Order");
                resultArray.put("Failed to create Order");
            }
            closeConnection();
        }
        return resultArray.toString();
    }

    public static void createOrder(String orderDate) throws SQLException{
        PreparedStatement orderpstmt = c.prepareStatement("INSERT INTO ORDERS(orderDate) VALUES(?)");
        orderpstmt.setString(1, orderDate);
        orderpstmt.executeUpdate();
    }

    public static void createItems(String itemName, String itemUnitPrice, String itemQuantity) throws SQLException{
        PreparedStatement itempstmt = c.prepareStatement("INSERT INTO ITEMS(ORDERID, ITEMNAME, ITEMUNITPRICE, ITEMQUANTITY) VALUES((SELECT seq from sqlite_sequence where name=\"orders\"),?,?,?)");
        itempstmt.setString(1, itemName);
        itempstmt.setString(2, itemUnitPrice);
        itempstmt.setString(3, itemQuantity);
        itempstmt.executeUpdate();
    }

    public static String getAllOrders(){
        JSONArray result = new JSONArray();
        String orderQuery = "SELECT * FROM ORDERS";
        String itemQuery = "SELECT * from ITEMS WHERE OrderId = ?";
        try{
            openConnection();
            PreparedStatement opstmt = c.prepareStatement(orderQuery);
            PreparedStatement ipstmt = c.prepareStatement(itemQuery);
            ResultSet rset = opstmt.executeQuery();
            while(rset.next()){
                JSONObject ordObj = new JSONObject();
                ordObj.put("orderId", rset.getInt("orderId"));
                ordObj.put("orderDate", rset.getString("orderDate"));
                ordObj.put("orderStatus", rset.getString("orderStatus"));
                JSONArray items = new JSONArray();
                ipstmt.setString(1, Integer.toString(rset.getInt("orderId")));
                ResultSet irset = ipstmt.executeQuery();
                while(irset.next()){
                    JSONObject itemObj = new JSONObject();
                    itemObj.put("itemId",irset.getInt("itemId"));
                    itemObj.put("itemName", irset.getString("itemName"));
                    itemObj.put("itemUnitPrice",irset.getString("itemUnitPrice"));
                    itemObj.put("itemQuantity", Integer.parseInt(irset.getString("itemQuantity")));
                    items.put(itemObj);
                }
                ordObj.put("items", items);
                result.put(ordObj);
            }
        }
        catch(SQLException e){
            System.out.println("Unable to fetch Data");
        }
        finally{
            closeConnection();
        }
        return result.toString();
    }

    public static String getOrder(int orderId){
        JSONObject ordObj = new JSONObject();
        try{
            openConnection();
            PreparedStatement opstmt = c.prepareStatement("SELECT * FROM ORDERS WHERE ORDERID = ?");
            opstmt.setInt(1, orderId);
            ResultSet orset = opstmt.executeQuery();
            ordObj.put("orderId", orset.getInt("orderId"));
            ordObj.put("orderDate", orset.getString("orderDate"));
            ordObj.put("orderStatus", orset.getString("orderStatus"));
            PreparedStatement ipstmt = c.prepareStatement("SELECT * FROM ITEMS WHERE ORDERID = ?");
            ipstmt.setInt(1, orderId);
            ResultSet irset = ipstmt.executeQuery();
            JSONArray iarr = new JSONArray();
            while(irset.next()){
                JSONObject itemObj = new JSONObject();
                itemObj.put("itemId",irset.getInt("itemId"));
                itemObj.put("itemName", irset.getString("itemName"));
                itemObj.put("itemUnitPrice",irset.getString("itemUnitPrice"));
                itemObj.put("itemQuantity", Integer.parseInt(irset.getString("itemQuantity")));
                iarr.put(itemObj);
            }
            ordObj.put("items", iarr);
        }
        catch(SQLException e){

        }
        finally{
            closeConnection();
        }
        return ordObj.toString();
    }

    public static void closeConnection(){
        try{
            c.close();
        }
        catch(SQLException e){
            System.out.println("Failed to close connection");
        }
    }
}
