import React, { useState, useEffect } from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet, ActivityIndicator, Image, Dimensions, ScrollView } from 'react-native';
import { database } from '../firebaseConfig';
import { ref, onValue } from 'firebase/database';
import { Ionicons } from '@expo/vector-icons';

const { width } = Dimensions.get('window');

const TYPE_COLORS = {
  'Travel': '#4CAF50',
  'Equipment': '#9C27B0',
  'Services': '#FF9800',
  'Materials': '#2196F3',
  'Software/Licenses': '#E91E63',
  'Labour costs': '#FFC107',
  'Utilities': '#00BCD4',
  'Miscellaneous': '#9E9E9E'
};

export default function ExpenseListScreen({ route, navigation }) {
  const { project, username } = route.params;
  const [expenses, setExpenses] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const expensesRef = ref(database, `users/${username}/projects/${project.firebaseId}/expenses`);
    
    const unsubscribe = onValue(expensesRef, (snapshot) => {
      if (snapshot.exists()) {
        const data = snapshot.val();
        const loadedExpenses = Object.keys(data).map(key => ({
          firebaseId: key,
          ...data[key]
        }));
        // Sort expenses by date descending
        loadedExpenses.sort((a, b) => new Date(b.date) - new Date(a.date));
        setExpenses(loadedExpenses);
      } else {
        setExpenses([]);
      }
      setLoading(false);
    }, (error) => {
      console.error("Firebase fetch error:", error);
      setLoading(false);
    });

    return () => unsubscribe();
  }, [username, project.firebaseId]);

  // Analytics Calculations
  const totalSpent = expenses.reduce((sum, exp) => sum + (parseFloat(exp.amount) || 0), 0);
  const budget = parseFloat(project.budget) || 0;
  const spentPercentage = budget > 0 ? (totalSpent / budget) * 100 : 0;
  const isOverBudget = totalSpent > budget;
  
  const spendingByType = expenses.reduce((acc, exp) => {
    const type = exp.type || 'Miscellaneous';
    acc[type] = (acc[type] || 0) + (parseFloat(exp.amount) || 0);
    return acc;
  }, {});

  const renderExpenseItem = ({ item }) => (
    <TouchableOpacity 
      style={styles.expenseCard}
      onPress={() => navigation.navigate('ExpenseDetail', { expense: item })}
      activeOpacity={0.7}
    >
      <View style={styles.expenseIconContainer}>
        <View style={[styles.typeIndicator, { backgroundColor: TYPE_COLORS[item.type] || TYPE_COLORS['Miscellaneous'] }]} />
      </View>
      <View style={styles.expenseInfo}>
        <Text style={styles.expenseType}>{item.type}</Text>
        <Text style={styles.expenseDetails}>{item.date} • {item.paymentMethod}</Text>
      </View>
      <View style={styles.expenseAmountContainer}>
        <Text style={styles.expenseAmount}>-{parseFloat(item.amount).toLocaleString()} {item.currency}</Text>
        <Text style={styles.paymentStatus}>{item.paymentStatus}</Text>
      </View>
    </TouchableOpacity>
  );

  const renderHeader = () => (
    <View style={styles.headerContainer}>
      {/* Minimal Project Info */}
      <View style={styles.minimalHeader}>
        <View style={styles.projectImageSmallContainer}>
          {project.photoUrl ? (
            <Image source={{ uri: project.photoUrl }} style={styles.projectImageSmall} />
          ) : (
            <View style={styles.placeholderImageSmall}>
              <Ionicons name="business" size={24} color="#6200EE" />
            </View>
          )}
        </View>
        <View style={styles.headerTextContainer}>
          <Text style={styles.minimalProjectName}>{project.projectName}</Text>
          <Text style={styles.minimalProjectDetails}>{project.projectCode} • {project.manager}</Text>
        </View>
        <View style={[
          styles.minimalStatus, 
          project.status === 'Completed' ? styles.statusCompleted : styles.statusActive
        ]}>
           <Text style={styles.statusText}>{project.status || 'Active'}</Text>
        </View>
      </View>

      {/* Budget Overview Card */}
      <View style={styles.analyticsCard}>
        <Text style={styles.analyticsLabel}>Total Expenses</Text>
        <Text style={styles.totalAmount}>${totalSpent.toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})}</Text>
        
        <View style={styles.budgetProgressInfo}>
          <Text style={styles.budgetText}>
            Budget: <Text style={styles.budgetBold}>${budget.toLocaleString()}</Text>
          </Text>
          <Text style={[styles.percentageText, isOverBudget && { color: '#FF5252' }]}>{spentPercentage.toFixed(1)}% used</Text>
        </View>

        {/* Stacked Spending Bar */}
        <View style={styles.stackedBarContainer}>
          {Object.keys(spendingByType).length > 0 ? (
            Object.keys(spendingByType).map((type, index) => {
              const widthPerc = (spendingByType[type] / totalSpent) * 100;
              if (widthPerc < 1) return null; // Skip very small segments
              return (
                <View 
                  key={type} 
                  style={[
                    styles.barSegment, 
                    { 
                      width: `${widthPerc}%`, 
                      backgroundColor: TYPE_COLORS[type] || TYPE_COLORS['Miscellaneous'],
                      borderTopLeftRadius: index === 0 ? 6 : 0,
                      borderBottomLeftRadius: index === 0 ? 6 : 0,
                      borderTopRightRadius: index === Object.keys(spendingByType).length - 1 ? 6 : 0,
                      borderBottomRightRadius: index === Object.keys(spendingByType).length - 1 ? 6 : 0,
                    }
                  ]} 
                />
              );
            })
          ) : (
            <View style={styles.emptyBar} />
          )}
        </View>

        {/* Legend */}
        <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.legendScroll}>
          {Object.keys(spendingByType).map(type => (
            <View key={type} style={styles.legendItem}>
              <View style={[styles.legendDot, { backgroundColor: TYPE_COLORS[type] || TYPE_COLORS['Miscellaneous'] }]} />
              <Text style={styles.legendText}>{type}</Text>
            </View>
          ))}
        </ScrollView>

        <Text style={styles.remarkText}>
          {isOverBudget ? "🚨 ALERT: You have exceeded the budget!" :
           spentPercentage > 90 ? "⚠️ You are close to exceeding the budget!" : 
           spentPercentage > 50 ? "💡 Spending is at a moderate level." : 
           "✅ Spending is well within the budget."}
        </Text>
      </View>

      <Text style={styles.sectionTitle}>Recent Transactions</Text>
    </View>
  );

  return (
    <View style={styles.container}>
      {loading ? (
        <ActivityIndicator size="large" color="#6200EE" style={styles.loader} />
      ) : (
        <FlatList
          data={expenses}
          keyExtractor={(item) => item.firebaseId}
          renderItem={renderExpenseItem}
          ListHeaderComponent={renderHeader}
          contentContainerStyle={styles.listContent}
          ListEmptyComponent={
            <View style={styles.emptyContainer}>
              <Ionicons name="receipt-outline" size={48} color="#ccc" />
              <Text style={styles.emptyText}>No expenses logged yet.</Text>
            </View>
          }
        />
      )}

      <TouchableOpacity 
        style={styles.fab}
        onPress={() => navigation.navigate('AddExpense', { project, username })}
      >
        <Ionicons name="add" size={32} color="#fff" />
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F8F9FE',
  },
  loader: {
    marginTop: 40,
  },
  listContent: {
    paddingBottom: 100,
  },
  headerContainer: {
    padding: 20,
  },
  minimalHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 24,
    backgroundColor: '#fff',
    padding: 12,
    borderRadius: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05,
    shadowRadius: 10,
    elevation: 2,
  },
  projectImageSmallContainer: {
    width: 50,
    height: 50,
    borderRadius: 15,
    overflow: 'hidden',
    backgroundColor: '#F0E6FF',
  },
  projectImageSmall: {
    width: '100%',
    height: '100%',
  },
  placeholderImageSmall: {
    width: '100%',
    height: '100%',
    justifyContent: 'center',
    alignItems: 'center',
  },
  headerTextContainer: {
    flex: 1,
    marginLeft: 12,
  },
  minimalProjectName: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#1A1A1A',
    fontFamily: 'Roboto',
  },
  minimalProjectDetails: {
    fontSize: 13,
    color: '#777',
    fontFamily: 'Roboto',
  },
  minimalStatus: {
    paddingHorizontal: 10,
    paddingVertical: 5,
    borderRadius: 10,
  },
  statusText: {
    fontSize: 11,
    fontWeight: 'bold',
    fontFamily: 'Roboto',
  },
  statusActive: { backgroundColor: '#DCFCE7', color: '#166534' },
  statusCompleted: { backgroundColor: '#DBEAFE', color: '#1E40AF' },
  statusOnHold: { backgroundColor: '#F3F4F6', color: '#374151' },
  
  analyticsCard: {
    backgroundColor: '#fff',
    borderRadius: 24,
    padding: 24,
    shadowColor: '#6200EE',
    shadowOffset: { width: 0, height: 10 },
    shadowOpacity: 0.1,
    shadowRadius: 20,
    elevation: 5,
    marginBottom: 24,
  },
  analyticsLabel: {
    fontSize: 14,
    color: '#777',
    fontWeight: '600',
    textTransform: 'uppercase',
    letterSpacing: 1,
    fontFamily: 'Roboto',
  },
  totalAmount: {
    fontSize: 36,
    fontWeight: '800',
    color: '#1A1A1A',
    marginVertical: 8,
    fontFamily: 'Roboto',
  },
  budgetProgressInfo: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: 8,
    marginBottom: 16,
  },
  budgetText: {
    fontSize: 14,
    color: '#555',
    fontFamily: 'Roboto',
  },
  budgetBold: {
    fontWeight: 'bold',
    color: '#333',
    fontFamily: 'Roboto',
  },
  percentageText: {
    fontSize: 14,
    fontWeight: 'bold',
    color: '#6200EE',
    fontFamily: 'Roboto',
  },
  stackedBarContainer: {
    height: 12,
    flexDirection: 'row',
    backgroundColor: '#F0F0F0',
    borderRadius: 6,
    overflow: 'hidden',
    marginBottom: 16,
  },
  barSegment: {
    height: '100%',
  },
  emptyBar: {
    flex: 1,
    backgroundColor: '#F0F0F0',
  },
  legendScroll: {
    marginBottom: 16,
  },
  legendItem: {
    flexDirection: 'row',
    alignItems: 'center',
    marginRight: 16,
  },
  legendDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    marginRight: 6,
  },
  legendText: {
    fontSize: 12,
    color: '#666',
    fontWeight: '500',
    fontFamily: 'Roboto',
  },
  remarkText: {
    fontSize: 13,
    color: '#555',
    fontWeight: 'bold',
    textAlign: 'center',
    paddingTop: 8,
    borderTopWidth: 1,
    borderTopColor: '#F0F0F0',
    fontFamily: 'Roboto',
  },
  sectionTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#1A1A1A',
    marginBottom: 16,
    fontFamily: 'Roboto',
  },
  expenseCard: {
    backgroundColor: '#fff',
    flexDirection: 'row',
    alignItems: 'center',
    padding: 16,
    marginHorizontal: 20,
    marginBottom: 12,
    borderRadius: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.03,
    shadowRadius: 8,
    elevation: 1,
  },
  typeIndicator: {
    width: 4,
    height: 40,
    borderRadius: 2,
  },
  expenseIconContainer: {
    marginRight: 12,
  },
  expenseInfo: {
    flex: 1,
  },
  expenseType: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#1A1A1A',
    marginBottom: 4,
    fontFamily: 'Roboto',
  },
  expenseDetails: {
    fontSize: 13,
    color: '#999',
    fontFamily: 'Roboto',
  },
  expenseAmountContainer: {
    alignItems: 'flex-end',
  },
  expenseAmount: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#FF5252',
    marginBottom: 4,
    fontFamily: 'Roboto',
  },
  paymentStatus: {
    fontSize: 11,
    color: '#6200EE',
    fontWeight: 'bold',
    textTransform: 'uppercase',
    fontFamily: 'Roboto',
  },
  emptyContainer: {
    padding: 40,
    alignItems: 'center',
  },
  emptyText: {
    fontSize: 16,
    color: '#999',
    marginTop: 12,
    fontFamily: 'Roboto',
  },
  fab: {
    position: 'absolute',
    right: 24,
    bottom: 24,
    backgroundColor: '#6200EE',
    width: 64,
    height: 64,
    borderRadius: 20,
    justifyContent: 'center',
    alignItems: 'center',
    shadowColor: '#6200EE',
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.4,
    shadowRadius: 12,
    elevation: 8,
  }
});
