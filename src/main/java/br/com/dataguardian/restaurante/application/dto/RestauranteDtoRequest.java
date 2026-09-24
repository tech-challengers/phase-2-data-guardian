package br.com.dataguardian.restaurante.application.dto;

public record RestauranteDtoRequest(
         String nome,
         String endereco,
         String gastronomia,
         String horarioFuncionamento,
         Long donoId) {
}
