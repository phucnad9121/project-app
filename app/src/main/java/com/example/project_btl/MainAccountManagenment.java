package com.example.project_btl;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainAccountManagenment extends AppCompatActivity {

    private RecyclerView recyclerUsers;
    private Button btnAdd, btnEdit, btnDelete;
    private List<User> userList;
    private UserAdapter adapter;

    private FirebaseFirestore db;  // Firestore instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        setContentView(R.layout.activity_account_management);

        recyclerUsers = findViewById(R.id.recyclerUsers);
        btnAdd = findViewById(R.id.btnAdd);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);

        // ✅ Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();

        userList = new ArrayList<>();
        adapter = new UserAdapter(this, userList);
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerUsers.setAdapter(adapter);

        // ✅ Lấy dữ liệu Firestore khi app khởi động
        fetchUsersFromFirestore();

        btnAdd.setOnClickListener(v -> openFormDialog(null));

        btnEdit.setOnClickListener(v -> {
            User selected = adapter.getSelectedUser();
            if (selected == null) {
                Toast.makeText(MainAccountManagenment.this, "Vui lòng chọn 1 người dùng để sửa", Toast.LENGTH_SHORT).show();
                return;
            }
            openFormDialog(selected);
        });

        btnDelete.setOnClickListener(v -> {
            User selected = adapter.getSelectedUser();
            if (selected == null) {
                Toast.makeText(MainAccountManagenment.this, "Vui lòng chọn 1 người dùng để xóa", Toast.LENGTH_SHORT).show();
                return;
            }
            confirmDelete(selected);
        });
    }

    // ✅ Hiển thị dialog thêm/sửa
    private void openFormDialog(User userToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_user, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        EditText edtUsername = view.findViewById(R.id.edtUsername);
        EditText edtName = view.findViewById(R.id.edtName);
        EditText edtEmail = view.findViewById(R.id.edtEmail);
        EditText edtPass = view.findViewById(R.id.edtPass);
        EditText edtPhone = view.findViewById(R.id.edtPhone);
        EditText edtAddress = view.findViewById(R.id.edtAddress);
        EditText edtGender = view.findViewById(R.id.edtGender);
        EditText edtRole = view.findViewById(R.id.edtRole);
        Button btnSave = view.findViewById(R.id.btnSave);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (userToEdit != null) {
            // Sửa → điền dữ liệu và chỉ đọc mật khẩu
            edtUsername.setText(userToEdit.getUsername());
            edtName.setText(userToEdit.getName());
            edtEmail.setText(userToEdit.getEmail());
            edtPhone.setText(userToEdit.getPhone());
            edtAddress.setText(userToEdit.getAddress());
            edtGender.setText(userToEdit.getGender());
            edtRole.setText(userToEdit.getRole());

            edtPass.setText(userToEdit.getPassword()); // hiển thị mật khẩu
            edtPass.setKeyListener(null);              // không cho nhập
            edtPass.setFocusable(false);
            edtPass.setCursorVisible(false);
        } else {
            // Thêm mới → mật khẩu có thể nhập
            edtPass.setText("");
            edtPass.setKeyListener(new EditText(this).getKeyListener());
            edtPass.setFocusable(true);
            edtPass.setCursorVisible(true);
        }

        btnSave.setOnClickListener(v -> {
            String username = edtUsername.getText().toString().trim();
            String name = edtName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String password = edtPass.getText().toString().trim(); // sửa: dùng getText()
            String phone = edtPhone.getText().toString().trim();
            String address = edtAddress.getText().toString().trim();
            String gender = edtGender.getText().toString().trim();
            String role = edtRole.getText().toString().trim();

            if (username.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập username và email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (userToEdit == null) {
                // Thêm mới
                if (password.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT).show();
                    return;
                }

                auth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener(authResult -> {
                            String uid = authResult.getUser().getUid();
                            User newUser = new User(username, name, email, password, phone, address, gender, role);
                            newUser.setId(uid);

                            db.collection("users").document(uid)
                                    .set(newUser)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Thêm tài khoản thành công", Toast.LENGTH_SHORT).show();
                                        fetchUsersFromFirestore();
                                        dialog.dismiss();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi lưu Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tạo tài khoản: " + e.getMessage(), Toast.LENGTH_SHORT).show());

            } else {
                // Sửa → chỉ cập nhật Firestore
                User updatedUser = new User(username, name, email, password, phone, address, gender, role);
                updatedUser.setId(userToEdit.getId());

                db.collection("users").document(userToEdit.getId())
                        .set(updatedUser)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Cập nhật người dùng thành công", Toast.LENGTH_SHORT).show();
                            fetchUsersFromFirestore();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });

        dialog.show();
    }



    // ✅ Xóa người dùng
    private void confirmDelete(User selected) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa người dùng này không?")
                .setPositiveButton("Xóa", (d, w) -> {
                    if (selected.getId() != null) {
                        db.collection("users").document(selected.getId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(MainAccountManagenment.this, "Xóa người dùng thành công", Toast.LENGTH_SHORT).show();
                                    fetchUsersFromFirestore();
                                })
                                .addOnFailureListener(e -> {
                                    e.printStackTrace();
                                    Toast.makeText(MainAccountManagenment.this, "Xóa thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // ✅ Lấy danh sách người dùng từ Firestore
    private void fetchUsersFromFirestore() {
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            User user = doc.toObject(User.class);
                            user.setId(doc.getId());
                            userList.add(user);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(MainAccountManagenment.this, "Lỗi khi tải dữ liệu: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}


