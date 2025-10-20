package com.example.project_btl;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class MainAccountManagenment extends AppCompatActivity implements UserAdapter.OnItemInteractionListener {

    private RecyclerView recyclerUsers;
    private Button btnAdd;
    private ImageButton btnBack; // Giữ lại từ P1

    // Bỏ btnEdit, btnDelete
    private List<User> userList;
    private UserAdapter adapter;

    // Khởi tạo Firebase/Firestore
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSupportActionBar() != null)
            getSupportActionBar().hide();

        setContentView(R.layout.activity_account_management);

        recyclerUsers = findViewById(R.id.recyclerUsers);
        btnAdd = findViewById(R.id.btnAdd);
        btnBack = findViewById(R.id.btnBack); // Ánh xạ btnBack

        // Khởi tạo Firebase/Firestore
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        userList = new ArrayList<>();
        // Khởi tạo adapter với listener là 'this' (theo P1)
        adapter = new UserAdapter(this, userList, this);
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerUsers.setAdapter(adapter);

        // Lấy dữ liệu từ Firestore
        fetchUsersFromFirestore();

        // Listener cho các nút
        btnAdd.setOnClickListener(v -> openFormDialog(null));
        btnBack.setOnClickListener(v -> finish());

        // Bỏ các listener cho btnEdit và btnDelete vì đã chuyển sang dùng click/long click trên item
    }

    // --- Implement Interface từ UserAdapter (Theo P1) ---

    @Override
    public void onItemClick(User user) {
        // Khi nhấn vào item -> mở form sửa
        openFormDialog(user);
    }

    @Override
    public void onItemLongClick(User user) {
        // Khi nhấn giữ item -> hiện dialog xác nhận xóa
        confirmDelete(user);
    }

    // ---------------------------------------------------

    private void openFormDialog(User userToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_user, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        EditText edtUsername = view.findViewById(R.id.edtUsername);
        EditText edtName = view.findViewById(R.id.edtName);
        EditText edtEmail = view.findViewById(R.id.edtEmail);

        // Giữ lại edtPass từ P2 cho chức năng thêm mới
        EditText edtPass = view.findViewById(R.id.edtPass);

        EditText edtPhone = view.findViewById(R.id.edtPhone);
        EditText edtAddress = view.findViewById(R.id.edtAddress);

        // Sử dụng Spinner cho Gender (Theo P1)
        Spinner spGender = view.findViewById(R.id.spGender);
        EditText edtRole = view.findViewById(R.id.edtRole);

        Button btnSave = view.findViewById(R.id.btnSave);

        // Khởi tạo Adapter cho Spinner Gender
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Nam", "Nữ"});
        spGender.setAdapter(genderAdapter);

        // Logic SỬA
        if (userToEdit != null) {
            edtUsername.setText(userToEdit.getUsername());
            edtName.setText(userToEdit.getName());
            edtEmail.setText(userToEdit.getEmail());
            edtPhone.setText(userToEdit.getPhone());
            edtAddress.setText(userToEdit.getAddress());
            edtRole.setText(userToEdit.getRole());

            // Set giá trị cho Spinner
            String gender = userToEdit.getGender();
            if (gender != null) {
                int spinnerPosition = genderAdapter.getPosition(gender);
                spGender.setSelection(spinnerPosition);
            }

            // Xử lý mật khẩu khi sửa (Theo P2)
            edtPass.setText(userToEdit.getPassword());
            edtPass.setKeyListener(null);
            edtPass.setFocusable(false);
            edtPass.setCursorVisible(false);

            // Khóa Username
            edtUsername.setEnabled(false);
        } else {
            // Logic THÊM MỚI
            edtPass.setText("");
            edtPass.setKeyListener(new EditText(this).getKeyListener());
            edtPass.setFocusable(true);
            edtPass.setCursorVisible(true);
            edtUsername.setEnabled(true);
        }


        btnSave.setOnClickListener(v -> {
            String username = edtUsername.getText().toString().trim();
            String name = edtName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String password = edtPass.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();
            String address = edtAddress.getText().toString().trim();
            String gender = spGender.getSelectedItem().toString(); // Lấy từ Spinner
            String role = edtRole.getText().toString().trim();

            if (username.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập username và email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (userToEdit == null) {
                // Thêm mới (Logic Firestore theo P2)
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
                // Cập nhật (Logic Firestore theo P2)
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


    // Xóa người dùng (Logic Firestore theo P2)
    private void confirmDelete(User userToDelete) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa người dùng '" + userToDelete.getName() + "' không?")
                .setPositiveButton("Xóa", (d, w) -> {
                    if (userToDelete.getId() != null) {
                        db.collection("users").document(userToDelete.getId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(MainAccountManagenment.this, "Xóa người dùng thành công", Toast.LENGTH_SHORT).show();
                                    fetchUsersFromFirestore(); // Tải lại dữ liệu sau khi xóa
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

    // Lấy danh sách người dùng từ Firestore (Theo P2)
    private void fetchUsersFromFirestore() {
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            User user = doc.toObject(User.class);
                            user.setId(doc.getId());
                            // Lấy mật khẩu từ Firestore
                            user.setPassword(doc.getString("password"));
                            userList.add(user);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(MainAccountManagenment.this, "Lỗi khi tải dữ liệu: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}