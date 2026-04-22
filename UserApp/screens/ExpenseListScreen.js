import React, { useState, useEffect } from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet, ActivityIndicator, Image } from 'react-native';
import { database } from '../firebaseConfig';
import { ref, onValue } from 'firebase/database';

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

  const renderExpenseItem = ({ item }) => (
    <TouchableOpacity 
      style={styles.expenseCard}
      onPress={() => navigation.navigate('ExpenseDetail', { expense: item })}
      activeOpacity={0.7}
    >
      <View style={styles.expenseHeader}>
        <Text style={styles.expenseType}>{item.type}</Text>
        <Text style={styles.expenseAmount}>{item.amount} {item.currency}</Text>
      </View>
      <Text style={styles.expenseDetails}>{item.expenseCode} • {item.date}</Text>
      <Text style={styles.expenseDetails}>Paid by: {item.paymentMethod} • Status: {item.paymentStatus}</Text>
      {item.description ? <Text style={styles.expenseDescription}>{item.description}</Text> : null}
    </TouchableOpacity>
  );

  const renderHeader = () => (
    <View style={styles.projectHeaderCard}>
      {project.photoUrl ? (
        <Image source={{ uri: project.photoUrl }} style={styles.projectImage} resizeMode="cover" />
      ) : (
        <View style={styles.placeholderImage}>
          <Text style={styles.placeholderText}>No Image Available</Text>
        </View>
      )}
      <View style={styles.projectInfo}>
        <View style={styles.titleRow}>
          <Text style={styles.projectCode}>{project.projectCode || 'N/A'}</Text>
          <Text style={[styles.statusBadge, project.status === 'Completed' ? styles.statusCompleted : styles.statusActive]}>
            {project.status || 'Active'}
          </Text>
        </View>
        <Text style={styles.projectName}>{project.projectName}</Text>
        <Text style={styles.projectManager}>Manager: {project.manager}</Text>
        <Text style={styles.projectDates}>{project.startDate} to {project.endDate}</Text>
      </View>
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
              <Text style={styles.emptyText}>No expenses logged yet.</Text>
            </View>
          }
        />
      )}

      {/* Floating Action Button */}
      <TouchableOpacity 
        style={styles.fab}
        onPress={() => navigation.navigate('AddExpense', { project, username })}
      >
        <Text style={styles.fabIcon}>+</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F5F7FA',
  },
  loader: {
    marginTop: 40,
  },
  listContent: {
    padding: 16,
    paddingBottom: 80, // space for FAB
  },
  projectHeaderCard: {
    backgroundColor: '#fff',
    borderRadius: 16,
    overflow: 'hidden',
    marginBottom: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.08,
    shadowRadius: 10,
    elevation: 3,
  },
  projectImage: {
    width: '100%',
    height: 180,
  },
  placeholderImage: {
    width: '100%',
    height: 150,
    backgroundColor: '#E0E0E0',
    justifyContent: 'center',
    alignItems: 'center',
  },
  placeholderText: {
    color: '#888',
    fontSize: 14,
  },
  projectInfo: {
    padding: 16,
  },
  titleRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  projectCode: {
    fontSize: 12,
    fontWeight: 'bold',
    color: '#6200EE',
    backgroundColor: '#F0E6FF',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 6,
  },
  statusBadge: {
    fontSize: 12,
    fontWeight: 'bold',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 12,
    overflow: 'hidden',
  },
  statusActive: { backgroundColor: '#E3F2FD', color: '#1976D2' },
  statusCompleted: { backgroundColor: '#E8F5E9', color: '#388E3C' },
  projectName: {
    fontSize: 22,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 8,
  },
  projectManager: {
    fontSize: 14,
    color: '#555',
    marginBottom: 4,
  },
  projectDates: {
    fontSize: 13,
    color: '#888',
  },
  expenseCard: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
    borderLeftWidth: 4,
    borderLeftColor: '#6200EE',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 1,
  },
  expenseHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 6,
  },
  expenseType: {
    fontSize: 16,
    fontWeight: '600',
    color: '#333',
  },
  expenseAmount: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#D32F2F',
  },
  expenseDetails: {
    fontSize: 13,
    color: '#666',
    marginBottom: 2,
  },
  expenseDescription: {
    fontSize: 14,
    color: '#444',
    marginTop: 8,
    fontStyle: 'italic',
  },
  emptyContainer: {
    padding: 30,
    alignItems: 'center',
  },
  emptyText: {
    fontSize: 16,
    color: '#999',
  },
  fab: {
    position: 'absolute',
    right: 20,
    bottom: 24,
    backgroundColor: '#6200EE',
    width: 60,
    height: 60,
    borderRadius: 30,
    justifyContent: 'center',
    alignItems: 'center',
    shadowColor: '#6200EE',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
    elevation: 6,
  },
  fabIcon: {
    fontSize: 32,
    color: '#fff',
    fontWeight: 'bold',
    marginTop: -2,
  }
});
