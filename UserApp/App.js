import * as React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { SafeAreaProvider } from 'react-native-safe-area-context';

import LoginScreen from './screens/LoginScreen';
import ProjectListScreen from './screens/ProjectListScreen';
import ExpenseListScreen from './screens/ExpenseListScreen';
import ExpenseDetailScreen from './screens/ExpenseDetailScreen';
import AddExpenseScreen from './screens/AddExpenseScreen';

const Stack = createNativeStackNavigator();

export default function App() {
  return (
    <SafeAreaProvider>
      <NavigationContainer>
        <Stack.Navigator 
          initialRouteName="Login"
          screenOptions={{
            headerStyle: {
              backgroundColor: '#6200EE',
            },
            headerTintColor: '#fff',
            headerTitleStyle: {
              fontWeight: 'bold',
            },
          }}
        >
          <Stack.Screen 
            name="Login" 
            component={LoginScreen} 
            options={{ title: 'Sign In' }}
          />
          <Stack.Screen 
            name="ProjectList" 
            component={ProjectListScreen} 
            options={{ title: 'My Projects' }}
          />
          <Stack.Screen 
            name="ExpenseList" 
            component={ExpenseListScreen} 
            options={{ title: 'Project Details' }}
          />
          <Stack.Screen 
            name="ExpenseDetail" 
            component={ExpenseDetailScreen} 
            options={{ title: 'Expense Details' }}
          />
          <Stack.Screen 
            name="AddExpense" 
            component={AddExpenseScreen} 
            options={{ title: 'Add New Expense' }}
          />
        </Stack.Navigator>
      </NavigationContainer>
    </SafeAreaProvider>
  );
}
