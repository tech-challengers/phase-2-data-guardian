package br.com.dataguardian.restaurante.application.dto;

public record RestauranteDtoResponse(
         Long id,
         String nome,
         String endereco,
         String gastronomia,
         String horarioFuncionamento) {
}
