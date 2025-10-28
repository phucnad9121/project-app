package com.example.project_btl.admin;

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

import com.example.project_btl.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class MainAccountManagenment extends AppCompatActivity implements UserAdapter.OnItemInteractionListener {

    private RecyclerView recyclerUsers; // hiển thị danh sách user
    private Button btnAdd; // nút thêm
    private ImageButton btnBack; // nút quay về

    private List<User> userList; // dữ liệu nguồn
    private UserAdapter adapter; // UserAdapter nhận userList và listener

    private FirebaseFirestore db; // Firestore truy cập database(firestore)
    private FirebaseAuth auth; // FirebaseAuth dùng để tạo người dùng (email/password).

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSupportActionBar() != null)
            getSupportActionBar().hide();

        setContentView(R.layout.activity_account_management);

        recyclerUsers = findViewById(R.id.recyclerUsers);
        btnAdd = findViewById(R.id.btnAdd);
        btnBack = findViewById(R.id.btnBack);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        userList = new ArrayList<>();

        adapter = new UserAdapter(this, userList, this);
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerUsers.setAdapter(adapter);

        // Lấy dữ liệu từ Firestore
        // Tải danh sách user từ firestore thêm vào userlist
        fetchUsersFromFirestore();

        // Listener cho các nút
        btnAdd.setOnClickListener(v -> openFormDialog(null));
        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    public void onItemClick(User user) {
        openFormDialog(user);
    }

    @Override
    public void onItemLongClick(User user) {
        confirmDelete(user);
    }


    private void openFormDialog(User userToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this); // Tạo  Builder để xây dựng hộp thoại
        View view = getLayoutInflater().inflate(R.layout.dialog_add_user, null);
        builder.setView(view); // Gắn layout đó vào dialog
        AlertDialog dialog = builder.create(); // tạo dialog hoàn chỉnh

        EditText edtUsername = view.findViewById(R.id.edtUsername);
        EditText edtName = view.findViewById(R.id.edtName);
        EditText edtEmail = view.findViewById(R.id.edtEmail);
        EditText edtPass = view.findViewById(R.id.edtPass);
        EditText edtPhone = view.findViewById(R.id.edtPhone);
        EditText edtAddress = view.findViewById(R.id.edtAddress);
        Spinner spGender = view.findViewById(R.id.spGender);
        EditText edtRole = view.findViewById(R.id.edtRole);
        Button btnSave = view.findViewById(R.id.btnSave);

        // Khởi tạo Adapter trung gian đưa dữ liệu vào Spinner, đưa danh sách giới tính vào spinner
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, new String[]{"Nam", "Nữ"});
        spGender.setAdapter(genderAdapter); // gắn dữ liệu vào spinner

        // Logic SỬA
        if (userToEdit != null) {
            edtUsername.setText(userToEdit.getUsername()); // Lấy dữ liệu của user từ Firestore đã chọn và đổ vào form để người dùng sửa
            edtName.setText(userToEdit.getName());
            edtEmail.setText(userToEdit.getEmail());
            edtPhone.setText(userToEdit.getPhone());
            edtAddress.setText(userToEdit.getAddress());
            edtRole.setText(userToEdit.getRole());

            // Lấy giới tính hiện có
            String gender = userToEdit.getGender();
            if (gender != null) {
                int spinnerPosition = genderAdapter.getPosition(gender);
                spGender.setSelection(spinnerPosition);
            }

            // Khoá mật khẩu khi sửa
            edtPass.setText(userToEdit.getPassword());
            edtPass.setKeyListener(null);
            edtPass.setFocusable(false);
            edtPass.setCursorVisible(false);

        } else {
            // Logic THÊM MỚI
            edtPass.setText(""); // Mật khẩu để trống cho nhập mới
            edtPass.setKeyListener(new EditText(this).getKeyListener());
            edtPass.setFocusable(true);
            edtPass.setCursorVisible(true);
            edtUsername.setEnabled(true);
        }

        // Logic LƯU
        btnSave.setOnClickListener(v -> {
            // Lấy nội dung từ các edittext và spinner
            String username = edtUsername.getText().toString().trim();
            String name = edtName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String password = edtPass.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();
            String address = edtAddress.getText().toString().trim();
            String gender = spGender.getSelectedItem().toString();
            String role = edtRole.getText().toString().trim();

            if (username.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập username và email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (userToEdit == null) {
                if (password.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT).show();
                    return;
                }

                auth.createUserWithEmailAndPassword(email, password) // Gọi firebase auth để tạo 1 tài khoản login mới
                        .addOnSuccessListener(authResult -> {
                            String uid = authResult.getUser().getUid(); // lấy id do firebase auth tạo
                            User newUser = new User(username, name, email, password, phone, address, gender, role);
                            newUser.setId(uid);

                            // Ghi thông tin user vào firestore
                            db.collection("users")
                                    .document(uid)
                                    .set(newUser)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Thêm tài khoản thành công", Toast.LENGTH_SHORT).show();
                                        fetchUsersFromFirestore(); // load lại dữ liệu nếu thành công
                                        dialog.dismiss(); // đóng cửa sổ
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi lưu Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tạo tài khoản: " + e.getMessage(), Toast.LENGTH_SHORT).show());

            } else {
                // Cập nhật
                User updatedUser = new User(username, name, email, password, phone, address, gender, role);
                updatedUser.setId(userToEdit.getId());

                // ghi đè thông tin trong firestore
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

    // Xóa người dùng
    private void confirmDelete(User userToDelete) {
        // Tạo 1 giao diện hỏi xác nhận
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa") // Tiêu đề cho dialog
                .setMessage("Bạn có chắc muốn xóa người dùng '" + userToDelete.getName() + "' không?")
                .setPositiveButton("Xóa", (d, w) -> {
                    if (userToDelete.getId() != null) {
                        // ket noi vs firestore, ktra id user có tồn tại k
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
                }).setNegativeButton("Hủy", null).show();
    }

    // Lấy danh sách người dùng từ Firestore
    private void fetchUsersFromFirestore() {
        // vào user trong filestore
        db.collection("users").get().addOnCompleteListener(task -> {
            // ktra truy vấn có thành công không
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