package config

import (
	"fmt"
	"github.com/fsnotify/fsnotify"
	"github.com/spf13/viper"
	"go.uber.org/zap"
	"os"
	"service-client/pkg/circuitbreaker"
)

type ServerConfig struct {
	Port int `yaml:"port"`
}

type ClientConfig struct {
	Host           string                              `yaml:"host"`
	MaxConnections int                                 `yaml:"maxConnections"`
	Timeout        string                              `yaml:"timeout"`
	CircuitBreaker circuitbreaker.CircuitBreakerConfig `yaml:"circuitBreaker"`
}

type AppConfig struct {
	Server           ServerConfig `yaml:"server"`
	ServiceApiClient ClientConfig `yaml:"serviceApiClient"`
}

type DynamicConfigs struct {
}

type Configuration struct {
	AppConfig      AppConfig
	DynamicConfigs DynamicConfigs
}

func LoadConfig() *Configuration {
	configuration := &Configuration{}
	env := os.Getenv("ACTIVE_PROFILE")
	LoadAppConfig(configuration, env)
	LoadDynamicConfig(&configuration.DynamicConfigs, "dynamicConfigs", env)
	return configuration
}

func LoadAppConfig(config *Configuration, env string) {
	appConfigViper := viper.New()
	appConfigViper.AddConfigPath("./config")
	appConfigViper.SetConfigName(fmt.Sprintf("application-%s", env))
	appConfigViper.SetConfigType("yaml")
	if err := appConfigViper.ReadInConfig(); err != nil {
		zap.L().Panic("Error loading application config", zap.Error(err))
	}
	if err := appConfigViper.Unmarshal(&config.AppConfig); err != nil {
		zap.L().Panic("Error unmarshalling application config", zap.Error(err))
	}
	zap.L().Info("Application config loaded successfully", zap.Any("config", config.AppConfig))
}

func LoadDynamicConfig(config interface{}, configName string, env string) {
	configViper := viper.New()

	configViper.AddConfigPath(fmt.Sprintf("./config/%s", env))
	configViper.SetConfigType("json")
	configViper.SetConfigName(configName)
	configViper.WatchConfig()
	configViper.OnConfigChange(func(e fsnotify.Event) {
		if err := configViper.ReadInConfig(); err != nil {
			zap.L().Error(fmt.Sprintf("Error reading dynamic config %s", configName), zap.Error(err))
			return
		} else {
			zap.L().Info(fmt.Sprintf("Dynamic config %s updated", configName))
		}

		if err := configViper.Unmarshal(config); err != nil {
			zap.L().Error("Error unmarshalling dynamic config", zap.Error(err))
		} else {
			zap.L().Info(fmt.Sprintf("Dynamic config %s updated", configName))
		}
	})
	if err := configViper.ReadInConfig(); err != nil {
		zap.L().Panic(fmt.Sprintf("Error loading dynamic config %s", configName), zap.Error(err))
	}
	if err := configViper.Unmarshal(config); err != nil {
		zap.L().Panic(fmt.Sprintf("Error unmarshalling dynamic config %s", configName), zap.Error(err))
	} else {
		zap.L().Info(fmt.Sprintf("Dynamic config %s loaded successfully", configName), zap.Any("config", config))
	}
	zap.L().Info(fmt.Sprintf("Dynamic config %s loaded successfully", configName), zap.Any("config", config))
}
