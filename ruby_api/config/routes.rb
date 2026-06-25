Rails.application.routes.draw do
  get "up" => "rails/health#show", as: :rails_health_check

  namespace :api do
    namespace :admin do
      resources :users, only: [:index, :show, :create, :update, :destroy]
    end
    resources :the_loai, path: "the-loai"
    resources :dang_ky, path: "dang-ky" do
      member do
        patch :approve
        patch :reject
      end
    end
    resources :do_an, path: "do-an", only: [:index, :show]
    resources :tien_do, path: "tien-do"
    resources :bai_dang, path: "bai-dang"
    resources :danh_gia, path: "danh-gia"
  end
end
