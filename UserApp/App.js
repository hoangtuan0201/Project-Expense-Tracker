import * as React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { SafeAreaProvider } from 'react-native-safe-area-context';

import LoginScreen from './screens/LoginScreen';
import ProjectListScreen from './screens/ProjectListScreen';
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
            name="AddExpense" 
            component={AddExpenseScreen} 
            options={{ title: 'Add New Expense' }}
          />
        </Stack.Navigator>
      </NavigationContainer>
    </SafeAreaProvider>
  );
}
