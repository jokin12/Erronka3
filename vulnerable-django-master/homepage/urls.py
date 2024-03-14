from django.urls import path, include
from . import views

app_name = 'articles'

urlpatterns = [
    path('', views.indexHome, name='home'),
    path('share', views.share_article, name='share'),
    path('search', views.search_articles, name='search'),
    path('about', views.about, name='about'),
    path('<slug:slug>', views.article_details, name='detail'),

]
