package com.pventabase.login.service;

import com.pventabase.common.exception.DuplicateResourceException;
import com.pventabase.common.exception.InvalidStateException;
import com.pventabase.common.exception.ResourceNotFoundException;
import com.pventabase.login.dto.LoginRequestDTO;
import com.pventabase.login.dto.LoginResponseDTO;
import com.pventabase.login.dto.RegisterRequestDTO;
import com.pventabase.login.security.JwtTokenProvider;
import com.pventabase.usuarios.dto.UsuarioRequestDTO;
import com.pventabase.usuarios.entity.Usuario;
import com.pventabase.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO requestDTO) {
        Usuario usuario = usuarioRepository.findByEmail(requestDTO.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", requestDTO.getEmail()));

        if (!usuario.getActivo()) {
            throw new InvalidStateException("Usuario desactivado");
        }

        if (!passwordEncoder.matches(requestDTO.getPassword(), usuario.getPassword())) {
            throw new InvalidStateException("Credenciales invalidas");
        }

        String token = jwtTokenProvider.generateToken(usuario.getEmail(), usuario.getRol().name());

        return LoginResponseDTO.builder()
                .token(token)
                .tipo("Bearer")
                .email(usuario.getEmail())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .rol(usuario.getRol().name())
                .build();
    }

    public LoginResponseDTO register(RegisterRequestDTO requestDTO) {
        if (usuarioRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DuplicateResourceException("Usuario", "email", requestDTO.getEmail());
        }

        UsuarioRequestDTO usuarioRequest = new UsuarioRequestDTO();
        usuarioRequest.setNombre(requestDTO.getNombre());
        usuarioRequest.setApellido(requestDTO.getApellido());
        usuarioRequest.setEmail(requestDTO.getEmail());
        usuarioRequest.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        usuarioRequest.setRol(requestDTO.getRol());
        usuarioRequest.setTelefono(requestDTO.getTelefono());

        Usuario usuario = new Usuario();
        usuario.setNombre(usuarioRequest.getNombre());
        usuario.setApellido(usuarioRequest.getApellido());
        usuario.setEmail(usuarioRequest.getEmail());
        usuario.setPassword(usuarioRequest.getPassword());
        usuario.setRol(usuarioRequest.getRol() != null ? usuarioRequest.getRol() : com.pventabase.usuarios.entity.RolUsuario.ROLE_USER);
        usuario.setTelefono(usuarioRequest.getTelefono());

        usuario = usuarioRepository.save(usuario);

        String token = jwtTokenProvider.generateToken(usuario.getEmail(), usuario.getRol().name());

        return LoginResponseDTO.builder()
                .token(token)
                .tipo("Bearer")
                .email(usuario.getEmail())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .rol(usuario.getRol().name())
                .build();
    }
}
