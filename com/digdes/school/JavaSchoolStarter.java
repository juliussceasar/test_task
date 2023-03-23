package com.digdes.school;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaSchoolStarter {
    List<Map<String, Object>> data;
    public JavaSchoolStarter() {
        this.data = new ArrayList<>();
    }
    public List<Map<String, Object>> execute(String request) throws Exception {
        String task = request.split(" ")[0].toUpperCase();
        switch (task) {
            // INSERT VALUES 'lastName' = 'Федоров', 'id' = 3, 'age' = 30, 'active' = true
            // INSERT VALUES 'lastName' = 'Петров', 'id' = 2, 'age' = 35, 'active' = true
            // INSERT VALUES 'lastName' = 'Супов', 'id' = 1, 'age' = 11, 'active' = false
            case "INSERT":
                Pattern columnName = Pattern.compile("('?\\w+'?) ?=");
                Pattern columnValue = Pattern.compile("= ?('?(\\w|[а-яё]|[А-ЯЁ]|\\w*\\.?\\d*)+'?)");

                Matcher matcher1 = columnName.matcher(request);
                Matcher matcher2 = columnValue.matcher(request);

                List<String> array1 = new ArrayList<>();
                List<String> array2 = new ArrayList<>();
                while (matcher1.find()) {
                    array1.add(matcher1.group(1).replace("'", "")
                            .replace("‘", "").replace("’", ""));
                }
                while (matcher2.find()) {
                    String match = matcher2.group(1).replace("'", "")
                            .replace("‘", "").replace("’", "");
                    array2.add(match);
                }

                Map<String, Object> row = new HashMap<>();
                for (int i = 0; i < array1.size(); i++) { // заполняем поля в хэшмапе
                    row.put(array1.get(i).toLowerCase(), array2.get(i));
                }
                data.add(row);
                ArrayList<Map<String, Object>> array = new ArrayList<>();
                array.add(row);
                return array;
            case "UPDATE":
                // UPDATE VALUES ‘active’=false, ‘cost’=10.1 where ‘id’=3
                List<Map<String, Object>> finalList = new ArrayList<>();
                if (request.contains(" where ")) {
                    int index = request.indexOf("where");
                    String subRequest = request.substring(0, index); //values without where
                    String requestSub = request.substring(index); // where without values

                    Stack<String> stack1 = findMatches(requestSub);
                    Stack<String> stack = findMatches(subRequest);

                    checkColumns(stack1);

                    if (request.contains(" and ") || request.contains(" AND ")) {
                        List<Map<String, Object>> list = getItemsFromList((stack1.pop() + " " + stack1.pop() + " " + stack1.pop()).split(" ")); //1 условие
                        List<Map<String, Object>> list1 = getItemsFromList((stack1.pop() + " " + stack1.pop() + " " + stack1.pop()).split(" ")); //2 условие
                        for (Map<String, Object> item : list) {
                            if (list1.contains(item)) {
                                finalList = update(list, stack);
                                data.remove(item);
                            }
                        }
                    } else if (request.contains(" or ") || request.contains(" OR ")) {
                        List<Map<String, Object>> list = getItemsFromList((stack1.pop() + " " + stack1.pop() + " " + stack1.pop()).split(" ")); //1 условие
                        List<Map<String, Object>> list1 = getItemsFromList((stack1.pop() + " " + stack1.pop() + " " + stack1.pop()).split(" ")); //2 условие
                        finalList.addAll(list);
                        finalList.addAll(list1);
                        for (Map<String,Object> elem : list1) {
                            if (list.contains(elem)) {
                                finalList.remove(elem);
                            }
                        }
                        data.removeAll(finalList);
                        finalList = update(finalList, stack);

                    } else {
                        finalList = getItemsFromList((stack1.pop() + " " + stack1.pop() + " " + stack1.pop()).split(" "));
                        data.removeAll(finalList);
                        finalList = update(finalList, stack);
                    }
                    data.addAll(finalList);
                    return finalList; // измененные значения
                } else {
                    Stack<String> stack = findMatches(request);
                    finalList = update(data, stack);
                    data = finalList;
                }

                return finalList;
            case "SELECT":
                // SELECT WHERE ‘age’>=30 and ‘lastName’ ilike ‘%п%’
                if (request.split(" ").length == 1) {
                    return data;
                } else {
                    Stack<String> stack = findMatches(request);

                    finalList = new ArrayList<>();
                    checkColumns(stack);

                    while (!stack.isEmpty()) {
                        if (request.contains(" and ") || request.contains(" AND ")) {
                            List<Map<String, Object>> list = getItemsFromList((stack.pop() + " " + stack.pop() + " " + stack.pop()).split(" ")); //1 условие
                            List<Map<String, Object>> list1 = getItemsFromList((stack.pop() + " " + stack.pop() + " " + stack.pop()).split(" ")); //2 условие
                            for (Map<String, Object> item : list) {
                                if (list1.contains(item)) {
                                    finalList.add(item);
                                }
                            }
                        } else if (request.contains(" or ") || request.contains(" OR ")) {
                            List<Map<String, Object>> list = getItemsFromList((stack.pop() + " " + stack.pop() + " " + stack.pop()).split(" ")); //1 условие
                            List<Map<String, Object>> list1 = getItemsFromList((stack.pop() + " " + stack.pop() + " " + stack.pop()).split(" ")); //2 условие
                            finalList.addAll(list);
                            finalList.addAll(list1);

                            for (Map<String,Object> elem : list1) {
                                if (list.contains(elem)) {
                                    finalList.remove(elem);
                                }
                            }
                        } else {
                            finalList = getItemsFromList((stack.pop() + " " + stack.pop() + " " + stack.pop()).split(" "));
                        }
                    }
                    return finalList;
                }
            case "DELETE":
                // DELETE WHERE ‘id’=3
                finalList = new ArrayList<>(); // удаляемое
                Stack<String> stack = findMatches(request);

                if (request.contains(" and ") || request.contains(" AND ")) {
                    List<Map<String, Object>> list = getItemsFromList((stack.pop() + " " + stack.pop() + " " + stack.pop()).split(" ")); //1 условие
                    List<Map<String, Object>> list1 = getItemsFromList((stack.pop() + " " + stack.pop() + " " + stack.pop()).split(" ")); //2 условие
                    for (Map<String, Object> item : list) {
                        if (list1.contains(item)) {
                            finalList.add(item);
                        }
                    }
                } else if (request.contains(" or ") || request.contains(" OR ")) {
                    List<Map<String, Object>> list = getItemsFromList((stack.pop() + " " + stack.pop() + " " + stack.pop()).split(" ")); //1 условие
                    List<Map<String, Object>> list1 = getItemsFromList((stack.pop() + " " + stack.pop() + " " + stack.pop()).split(" ")); //2 условие
                    finalList.addAll(list);
                    finalList.addAll(list1);

                    for (Map<String,Object> elem : list1) {
                        if (list.contains(elem)) {
                            finalList.remove(elem);
                        }
                    }
                } else {
                    finalList.addAll(getItemsFromList((stack.pop() + " " + stack.pop() + " " + stack.pop()).split(" ")));
                }

                data.removeAll(finalList);
                return finalList;
            case "EXIT":
                System.exit(1);
            break;
        }
        return data;
    }

    public List<Map<String, Object>> getItemsFromList(String[] strings) { // age >= 30
        List<Map<String, Object>> fin = new ArrayList<>();
        try {
            strings[2] = strings[2].toLowerCase();
            for (Map<String, Object> datum : data) {
                switch (strings[1]) {
                    case (">="):
                        if (Integer.parseInt((String) datum.get(strings[2])) >= Integer.parseInt(strings[0])) {
                            fin.add(datum);
                        }
                        break;
                    case (">"):
                        if (Integer.parseInt((String) datum.get(strings[2])) > Integer.parseInt(strings[0])) {
                            fin.add(datum);
                        }
                        break;
                    case ("<"):
                        if (Integer.parseInt((String) datum.get(strings[2])) < Integer.parseInt(strings[0])) {
                            fin.add(datum);
                        }
                        break;
                    case ("<="):
                        if (Integer.parseInt((String) datum.get(strings[2])) <= Integer.parseInt(strings[0])) {
                            fin.add(datum);
                        }
                        break;
                    case ("="):
                        if (strings[0].equals(datum.get(strings[2]))) {
                            fin.add(datum);
                        }
                        break;
                    case ("!="):
                        if (!strings[0].equals(datum.get(strings[2]))) {
                            fin.add(datum);
                        }
                        break;
                    case ("ilike"):

                        if (datum.get(strings[2]).toString().contains(strings[0].toLowerCase()) ||
                                datum.get(strings[2]).toString().contains(strings[0].toUpperCase())) {
                            fin.add(datum);
                        }
                        break;
                    case ("like"):
                        if (datum.get(strings[2]).toString().contains(strings[0])) {
                            fin.add(datum);
                        }
                        break;
                }
            }
        } catch (NullPointerException | NumberFormatException e) {
            e.printStackTrace();
        }
        return fin;
    }

    public Stack<String> findMatches(String request) {
        Pattern where = Pattern.compile("(‘?'?\\w+’?'?) ?(>=|=|<=|!=|>|<|like|ilike) ?‘?'?%?(\\w*\\d*\\.?\\d*[А-ЯЁ]*[а-яё]*)%?’?'?");
        Matcher matcher = where.matcher(request);

        Stack<String> stack = new Stack<>();

        while (matcher.find()) {   // пока не нашли все условия, заполним хэшмапу fin
            stack.push(matcher.group(1).replace("‘", "").replace("’", "").replace("'", ""));
            stack.push(matcher.group(2));
            stack.push(matcher.group(3).replace("‘", "").replace("'", "")
                    .replace("%", "").replace("‘", ""));
        }
        return stack;
    }
    public List<Map<String,Object>> update(List<Map<String,Object>> finalList, Stack<String> stack) {
        for (Map<String, Object> stringObjectMap : finalList) {
            int t = 2;
            for (int m = 0; m < stack.size(); m += 3) { //атрибуты
                String attribute = stack.get(m);
                stringObjectMap.remove(attribute);
                if (stack.get(t).equals("null")) {
                    stringObjectMap.put(attribute.toLowerCase(), ""); //значение из ячейки удаляется
                } else {
                    stringObjectMap.put(attribute.toLowerCase(), stack.get(t));
                }
                t += 3;

            }

        }
        return finalList;
    }
    public void checkColumns(Stack<String> stack) throws Exception { // проверка на ненайденную колонку
        int count = 0;
        for (Map<String, Object> datum : data) {
            for (int i = 0; i < stack.size(); i+=3) {
                if (!datum.containsKey(stack.get(i).toLowerCase())) {
                    count++;
                }
            }
        }
        if (count == data.size()) {
            throw new Exception("Нет такой колонки!");
        }
    }

}
