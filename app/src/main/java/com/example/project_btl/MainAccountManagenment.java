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


import java.util.ArrayList;
import java.util.List;

public class MainAccountManagenment extends AppCompatActivity {
    private RecyclerView recyclerUsers;
    private Button btnAdd, btnEdit, btnDelete;
    private List<User> userList;
    private UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSupportActionBar() != null)
            getSupportActionBar().hide();

        setContentView(R.layout.activity_account_management);

        recyclerUsers = findViewById(R.id.recyclerUsers);
        btnAdd = findViewById(R.id.btnAdd);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);

        userList = new ArrayList<>();
        // Thêm dữ liệu demo (bỏ hoặc thay thế nếu dùng DB)
        userList.add(new User("vietanh", "Nguyễn Văn Việt Anh", "vietanh@gmail.com", "0123456789", "Hà Nội", "Nam", "Admin"));

        adapter = new UserAdapter(this, userList);
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerUsers.setAdapter(adapter);

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
            confirmDelete();
        });
    }

    private void openFormDialog(User userToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_user, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        EditText edtUsername = view.findViewById(R.id.edtUsername);
        EditText edtName = view.findViewById(R.id.edtName);
        EditText edtEmail = view.findViewById(R.id.edtEmail);
        EditText edtPhone = view.findViewById(R.id.edtPhone);
        EditText edtAddress = view.findViewById(R.id.edtAddress);
        EditText edtGender = view.findViewById(R.id.edtGender);
        EditText edtRole = view.findViewById(R.id.edtRole);
        Button btnSave = view.findViewById(R.id.btnSave);

        if (userToEdit != null) {
            // Điền sẵn thông tin để sửa
            edtUsername.setText(userToEdit.getUsername());
            edtName.setText(userToEdit.getName());
            edtEmail.setText(userToEdit.getEmail());
            edtPhone.setText(userToEdit.getPhone());
            edtAddress.setText(userToEdit.getAddress());
            edtGender.setText(userToEdit.getGender());
            edtRole.setText(userToEdit.getRole());

            // Nếu muốn khóa username để không đổi, enable = false
            // edtUsername.setEnabled(false);
        }

        btnSave.setOnClickListener(v -> {
            String username = edtUsername.getText().toString().trim();
            String name = edtName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();
            String address = edtAddress.getText().toString().trim();
            String gender = edtGender.getText().toString().trim();
            String role = edtRole.getText().toString().trim();

            if (username.isEmpty() || email.isEmpty()) {
                Toast.makeText(MainAccountManagenment.this, "Nhập ít nhất username và email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (userToEdit == null) {
                // Thêm mới
                User newUser = new User(username, name, email, phone, address, gender, role);
                adapter.addUser(newUser);
            } else {
                // Cập nhật vị trí đang chọn
                int pos = adapter.getSelectedPosition();
                User updated = new User(username, name, email, phone, address, gender, role);
                if (pos != RecyclerView.NO_POSITION) {
                    // bạn có thể giữ nguyên object để preserve reference; mình thay thế bằng updateSelected
                    adapter.updateSelected(updated);
                }
            }

            dialog.dismiss();
        });

        dialog.show();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa người dùng này không?")
                .setPositiveButton("Xóa", (d, w) -> adapter.removeSelected())
                .setNegativeButton("Hủy", null)
                .show();
    }
}
